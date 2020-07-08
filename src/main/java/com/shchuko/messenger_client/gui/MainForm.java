package com.shchuko.messenger_client.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.shchuko.messenger_client.dto.auth.GeneratedTokensDTO;
import com.shchuko.messenger_client.dto.auth.LoginRequestDTO;
import com.shchuko.messenger_client.dto.auth.SignUpRequestDTO;
import com.shchuko.messenger_client.dto.auth.TokensRefreshRequestDTO;
import com.shchuko.messenger_client.dto.mesenger.*;
import com.shchuko.messenger_client.security.TokenStorage;

import javax.swing.*;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.*;

public class MainForm extends JFrame {
    static final String API_ENDPOINT = "api/v1";

    static final String AUTH_ENDPOINT = API_ENDPOINT + "/auth";
    static final String LOGIN_ENDPOINT = AUTH_ENDPOINT + "/login";
    static final String SIGN_UP_ENDPOINT = AUTH_ENDPOINT + "/sign-up";
    static final String TOKENS_REFRESH_ENDPOINT = AUTH_ENDPOINT + "/tokens-refresh";

    static final String MESSENGER_ENDPOINT = API_ENDPOINT + "/messenger";
    static final String MESSAGES_ENDPOINT = MESSENGER_ENDPOINT + "/messages";
    static final String CHATS_ENDPOINT = MESSENGER_ENDPOINT + "/chats";

    private final Client client;

    public JPanel mainPanel;
    private JTextField urlField;
    private JComboBox<String> chatsComboBox;
    private JButton loginButton;
    private JTextField usernameField;
    private JTextField passwordField;
    private JTextField messageTextField;
    private JButton sendButton;
    private JLabel statusLabel;
    private JButton createChatOdAddMembersButton;
    private JTextField createChatName;
    private JTextField createChatMembers;
    private JTextArea messagesTextArea;
    private JScrollPane messagesScrollPane;
    private JButton signUpButton;
    private JButton logoutButton;

    private Timer sessionTokenRefreshTimer = new Timer();
    private Timer refreshTokenRefreshTimer = new Timer();
    private Timer updateRequestTimer = new Timer();

    private String serverUrl = "";
    private String username = "";
    private String password = "";

    private TokenStorage tokenStorage = new TokenStorage();

    private final Set<String> userChats = new HashSet<>();

    private String selectedChat = "";
    private long lastMessageTimestamp = -1;

    public MainForm(Client client) throws HeadlessException {
        this.client = client;

        $$$setupUI$$$();

        messagesTextArea.setLineWrap(true);
        messagesTextArea.setWrapStyleWord(true);
        messagesScrollPane.setViewportView(messagesTextArea);

        setMinimumSize(new Dimension(900, 442));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        pack();
        centring(this);
        setVisible(true);
        addActionListeners();
    }

    private void addActionListeners() {
        loginButton.addActionListener(actionEvent -> {
            serverUrl = urlField.getText();
            username = usernameField.getText();
            password = passwordField.getText();
            login();
        });

        signUpButton.addActionListener(actionEvent -> {
            serverUrl = urlField.getText();
            username = usernameField.getText();
            password = passwordField.getText();
            signUp();
        });

        logoutButton.addActionListener(actionEvent -> {
            onUserDisconnect();
        });

        chatsComboBox.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() != ItemEvent.SELECTED) {
                return;
            }

            String chatName = (String) itemEvent.getItem();
            if (chatName.isEmpty()) {
                setSelectedChat("");
            } else {
                setSelectedChat(chatName);
            }
        });

        sendButton.addActionListener(actionEvent -> sendMessage());

        createChatOdAddMembersButton.addActionListener(actionEvent -> {
            createChatOrAddMembers();
        });
    }

    private void login() {
        try {
            LoginRequestDTO requestDTO = new LoginRequestDTO();
            requestDTO.setPassword(password);
            requestDTO.setUsername(username);


            Response response = client.target(serverUrl)
                    .path(LOGIN_ENDPOINT)
                    .request(MediaType.APPLICATION_JSON).accept(MediaType.TEXT_PLAIN_TYPE)
                    .post(Entity.json(requestDTO));

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                GeneratedTokensDTO generatedTokensDTO = response.readEntity(GeneratedTokensDTO.class);

                username = requestDTO.getUsername();
                tokenStorage.updateSessionToken(generatedTokensDTO.getSessionToken(), generatedTokensDTO.getSessionExpiresOn());
                tokenStorage.updateRefreshToken(generatedTokensDTO.getRefreshToken(), generatedTokensDTO.getRefreshExpiresOn());
                statusLabel.setText("Logged in");
                onUserConnect();
                return;
            }

            onUserDisconnect();
            if (response.getStatus() == Response.Status.FORBIDDEN.getStatusCode()
                    || response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode()) {
                statusLabel.setText("Wrong username/password");
            } else {
                statusLabel.setText(response.getStatusInfo().getReasonPhrase());
            }
        } catch (ProcessingException e) {
            onUserDisconnect();
            statusLabel.setText("Couldn't connect");
        }
    }

    private void signUp() {
        try {
            SignUpRequestDTO requestDTO = new SignUpRequestDTO();
            requestDTO.setPassword(password);
            requestDTO.setUsername(username);

            Response response = client.target(serverUrl)
                    .path(SIGN_UP_ENDPOINT)
                    .request(MediaType.APPLICATION_JSON).accept(MediaType.TEXT_PLAIN_TYPE)
                    .post(Entity.json(requestDTO));

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                GeneratedTokensDTO generatedTokensDTO = response.readEntity(GeneratedTokensDTO.class);

                username = requestDTO.getUsername();
                tokenStorage.updateSessionToken(generatedTokensDTO.getSessionToken(), generatedTokensDTO.getSessionExpiresOn());
                tokenStorage.updateRefreshToken(generatedTokensDTO.getRefreshToken(), generatedTokensDTO.getRefreshExpiresOn());
                statusLabel.setText("Logged in");
                onUserConnect();
                return;
            }

            onUserDisconnect();
            if (response.getStatus() == Response.Status.FORBIDDEN.getStatusCode()
                    || response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode()) {
                statusLabel.setText("Incorrect username/password");
            } else {
                statusLabel.setText(response.getStatusInfo().getReasonPhrase());
            }
        } catch (ProcessingException e) {
            onUserDisconnect();
            statusLabel.setText("Couldn't connect");
        }
    }

    private void updateChats() {
        try {
            Response response = client.target(serverUrl)
                    .path(CHATS_ENDPOINT)
                    .request(MediaType.APPLICATION_JSON).accept(MediaType.TEXT_PLAIN_TYPE)
                    .header(getAuthHeaderName(), getAuthHeaderSessionValue())
                    .get();
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                GetUserChatsResponseDTO responseDTO = response.readEntity(GetUserChatsResponseDTO.class);

                for (ChatDTO chatDTO : responseDTO.getUserChats()) {
                    String chatName = chatDTO.getChatName();
                    if (chatName != null && !chatName.trim().isEmpty() && !userChats.contains(chatName)) {
                        chatsComboBox.addItem(chatName);
                        userChats.add(chatName);
                    }
                }

            } else {
                onUserDisconnect();
                statusLabel.setText(response.getStatusInfo().getReasonPhrase());
            }
        } catch (ProcessingException e) {
            onUserDisconnect();
            statusLabel.setText("Couldn't connect");
        }
    }

    private boolean updateMessages() {
        try {
            GetMessagesRequestDTO requestDTO = new GetMessagesRequestDTO();
            requestDTO.setChatName(selectedChat);
            if (lastMessageTimestamp == -1) {
                lastMessageTimestamp = new Date().getTime() / 1000;
                requestDTO.setAction(GetMessagesRequestDTO.SupportedActions.GET_MESSAGES_BEFORE_TIMESTAMP.name());
            } else {
                requestDTO.setAction(GetMessagesRequestDTO.SupportedActions.GET_MESSAGES_AFTER_TIMESTAMP.name());
            }
            requestDTO.setTimestamp(lastMessageTimestamp);

            Response response = client.target(serverUrl)
                    .path(MESSAGES_ENDPOINT)
                    .request(MediaType.APPLICATION_JSON).accept(MediaType.TEXT_PLAIN_TYPE)
                    .header(getAuthHeaderName(), getAuthHeaderSessionValue())
                    .put(Entity.json(requestDTO));

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                GetMessagesResponseDTO responseDTO = response.readEntity(GetMessagesResponseDTO.class);
                responseDTO.getMessages().forEach(this::printMessage);
                lastMessageTimestamp = new Date().getTime() / 1000;
                return true;
            } else {
                return false;
            }
        } catch (ProcessingException e) {
            onUserDisconnect();
            statusLabel.setText("Couldn't connect");
            return false;
        }
    }

    private void sendMessage() {
        try {
            SendMessageRequestDTO requestDTO = new SendMessageRequestDTO();
            requestDTO.setChatName(selectedChat);
            requestDTO.setMessageContent(messageTextField.getText());

            Response response = client.target(serverUrl)
                    .path(MESSAGES_ENDPOINT)
                    .request(MediaType.APPLICATION_JSON).accept(MediaType.TEXT_PLAIN_TYPE)
                    .header(getAuthHeaderName(), getAuthHeaderSessionValue())
                    .post(Entity.json(requestDTO));

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                messageTextField.setText("");
            } else {
                statusLabel.setText("Sending error: " + response.getStatusInfo().getReasonPhrase());
            }
        } catch (ProcessingException e) {
            onUserDisconnect();
            statusLabel.setText("Couldn't connect");
        }
    }

    private void createChatOrAddMembers() {
        if (userChats.contains(createChatName.getText())) {
            addChatMembers();
            return;
        }

        createChat();
    }

    private void createChat() {
        try {
            CreateChatRequestDTO requestDTO = new CreateChatRequestDTO();
            requestDTO.setChatName(createChatName.getText());
            requestDTO.setMembers(new HashSet<>(Arrays.asList(createChatMembers.getText().trim().split(";"))));

            Response response = client.target(serverUrl)
                    .path(CHATS_ENDPOINT)
                    .request(MediaType.APPLICATION_JSON).accept(MediaType.TEXT_PLAIN_TYPE)
                    .header(getAuthHeaderName(), getAuthHeaderSessionValue())
                    .post(Entity.json(requestDTO));

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                statusLabel.setText("Created");
            } else if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                statusLabel.setText("err." + response.readEntity(CreateChatResponseDTO.class).getStatusMessage());
            } else {
                statusLabel.setText(response.getStatusInfo().getReasonPhrase());
            }
        } catch (ProcessingException e) {
            onUserDisconnect();
            statusLabel.setText("Couldn't connect");
        }
    }

    private void addChatMembers() {
        try {
            AddChatMembersRequestDTO requestDTO = new AddChatMembersRequestDTO();
            requestDTO.setChatName(createChatName.getText());
            requestDTO.setMembers(new HashSet<>(Arrays.asList(createChatMembers.getText().trim().split("'"))));

            Response response = client.target(serverUrl)
                    .path(CHATS_ENDPOINT)
                    .request(MediaType.APPLICATION_JSON).accept(MediaType.TEXT_PLAIN_TYPE)
                    .header(getAuthHeaderName(), getAuthHeaderSessionValue())
                    .put(Entity.json(requestDTO));

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                statusLabel.setText("Created");
            } else if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                statusLabel.setText("err." + response.readEntity(CreateChatResponseDTO.class).getStatusMessage());
            } else {
                statusLabel.setText(response.getStatusInfo().getReasonPhrase());
            }
        } catch (ProcessingException e) {
            onUserDisconnect();
            statusLabel.setText("Couldn't connect");
        }
    }

    private String getAuthHeaderName() {
        return "Authorization";
    }

    private String getAuthHeaderSessionValue() {
        return "Bearer " + tokenStorage.getSessionToken();
    }

    private String getAuthHeaderRefreshValue() {
        return "Bearer " + tokenStorage.getRefreshToken();
    }

    private void setSelectedChat(String selectedChat) {
        messagesTextArea.setText("");
        this.selectedChat = selectedChat;
        this.lastMessageTimestamp = -1;

        if (selectedChat.isEmpty()) {
            makeChatNotWriteable();
            return;
        }

        if (updateMessages()) {
            makeChatWriteable();
        }
    }

    private void printMessage(MessageDTO messageDTO) {
        if (messageDTO == null) {
            return;
        }

        Date timestamp = new Date(messageDTO.getTimestamp() * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");

        String stringBuilder = System.lineSeparator() + messageDTO.getAuthor() + " " +
                simpleDateFormat.format(timestamp) + " " +
                messageDTO.getContent();
        messagesTextArea.append(stringBuilder);
    }

    private void onUserConnect() {
        runTokensRefreshTimers();
        runUpdateRequests();

        userChats.clear();

        chatsComboBox.setEnabled(true);
        chatsComboBox.addItem("");


        loginButton.setEnabled(false);
        signUpButton.setEnabled(false);
        logoutButton.setEnabled(true);

        createChatMembers.setEnabled(true);
        createChatName.setEnabled(true);
        createChatOdAddMembersButton.setEnabled(true);

    }

    private void onUserDisconnect() {
        tokenStorage.clear();

        stopUpdateRequests();

        stopTokensRefreshTimers();

        makeChatNotWriteable();

        userChats.clear();
        chatsComboBox.removeAllItems();
        chatsComboBox.setEnabled(false);
        statusLabel.setText("Disconnected");

        loginButton.setEnabled(true);
        signUpButton.setEnabled(true);
        logoutButton.setEnabled(false);

        createChatMembers.setEnabled(false);
        createChatName.setEnabled(false);
        createChatOdAddMembersButton.setEnabled(false);
    }

    private void makeChatWriteable() {
        messageTextField.setEnabled(true);
        sendButton.setEnabled(true);
    }

    private void makeChatNotWriteable() {
        messagesTextArea.setText("");
        messageTextField.setText("");
        messageTextField.setEnabled(false);
        sendButton.setEnabled(false);
    }

    private static void centring(JFrame frame) {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width / 2 - frame.getWidth() / 2,
                dim.height / 2 - frame.getHeight() / 2);
    }

    private TimerTask getRefreshTask(boolean refreshRefreshToken) {
        return new TimerTask() {
            @Override
            public void run() {
                System.out.println("Refresh task: " + refreshRefreshToken);
                refreshTokens(refreshRefreshToken);
            }
        };
    }

    private void runTokensRefreshTimers() {
        sessionTokenRefreshTimer.schedule(getRefreshTask(false), tokenStorage.getSessionTokenRefreshOn());
        refreshTokenRefreshTimer.schedule(getRefreshTask(true), tokenStorage.getRefreshTokenRefreshOn());
    }

    private void stopTokensRefreshTimers() {
        sessionTokenRefreshTimer.cancel();
        sessionTokenRefreshTimer = new Timer();
//        sessionTokenRefreshTimer.purge();

        refreshTokenRefreshTimer.cancel();
        refreshTokenRefreshTimer = new Timer();
//        refreshTokenRefreshTimer.purge();
    }

    private void refreshTokens(boolean refreshRefreshToken) {
        try {
            TokensRefreshRequestDTO requestDTO = new TokensRefreshRequestDTO();
            requestDTO.setOldSessionToken(tokenStorage.getSessionToken());
            requestDTO.setNewRefreshTokenNeeded(refreshRefreshToken);

            Response response = client.target(serverUrl)
                    .path(TOKENS_REFRESH_ENDPOINT)
                    .request(MediaType.APPLICATION_JSON).accept(MediaType.TEXT_PLAIN_TYPE)
                    .header(getAuthHeaderName(), getAuthHeaderRefreshValue())
                    .post(Entity.json(requestDTO));

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                GeneratedTokensDTO generatedTokensDTO = response.readEntity(GeneratedTokensDTO.class);

                tokenStorage.updateSessionToken(generatedTokensDTO.getSessionToken(), generatedTokensDTO.getSessionExpiresOn());
                sessionTokenRefreshTimer.schedule(getRefreshTask(false), tokenStorage.getSessionTokenRefreshOn());
                if (refreshRefreshToken) {
                    tokenStorage.updateRefreshToken(generatedTokensDTO.getRefreshToken(), generatedTokensDTO.getRefreshExpiresOn());
                    refreshTokenRefreshTimer.schedule(getRefreshTask(true), tokenStorage.getRefreshTokenRefreshOn());
                }
                return;
            }

            onUserDisconnect();
            statusLabel.setText("Tkn refr err: " + response.getStatusInfo().getReasonPhrase());
        } catch (ProcessingException e) {
            onUserDisconnect();
            statusLabel.setText("Couldn't connect");
        }
    }

    private void runUpdateRequests() {
        final long START_IN = 0L;
        final long PERIOD = 2000L;

        updateRequestTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateChats();
                if (selectedChat != null && !selectedChat.isEmpty()) {
                    updateMessages();
                }
            }
        }, START_IN, PERIOD);
    }

    private void stopUpdateRequests() {
        updateRequestTimer.cancel();
        updateRequestTimer = new Timer();
//        updateRequestTimer.purge();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        mainPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("URL:");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        urlField = new JTextField();
        panel2.add(urlField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        separator1.setOrientation(1);
        panel3.add(separator1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Response:");
        panel5.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        statusLabel = new JLabel();
        statusLabel.setText("<None>");
        panel5.add(statusLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        messagesTextArea = new JTextArea();
        messagesTextArea.setEditable(false);
        messagesTextArea.setText("");
        panel6.add(messagesTextArea, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 50), null, 0, false));
        messagesScrollPane = new JScrollPane();
        messagesScrollPane.setHorizontalScrollBarPolicy(31);
        panel6.add(messagesScrollPane, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel7, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Chat");
        panel8.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chatsComboBox = new JComboBox();
        chatsComboBox.setEditable(false);
        chatsComboBox.setEnabled(false);
        panel8.add(chatsComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel9, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        messageTextField = new JTextField();
        messageTextField.setEditable(true);
        messageTextField.setEnabled(false);
        panel9.add(messageTextField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        sendButton = new JButton();
        sendButton.setEnabled(false);
        sendButton.setText("Send");
        panel9.add(sendButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel10, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel10.add(panel11, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel11.add(panel12, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        usernameField = new JTextField();
        panel12.add(usernameField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Username:");
        panel12.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel11.add(panel13, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        passwordField = new JTextField();
        panel13.add(passwordField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Password:");
        panel13.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel14 = new JPanel();
        panel14.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel11.add(panel14, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        loginButton = new JButton();
        loginButton.setText("Login");
        panel14.add(loginButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        signUpButton = new JButton();
        signUpButton.setText("Sign up");
        panel14.add(signUpButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        logoutButton = new JButton();
        logoutButton.setEnabled(false);
        logoutButton.setText("Logout");
        panel14.add(logoutButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel15 = new JPanel();
        panel15.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel10.add(panel15, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel16 = new JPanel();
        panel16.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel15.add(panel16, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        createChatOdAddMembersButton = new JButton();
        createChatOdAddMembersButton.setEnabled(false);
        createChatOdAddMembersButton.setText("Create chat / add chat members");
        panel16.add(createChatOdAddMembersButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel17 = new JPanel();
        panel17.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel15.add(panel17, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel18 = new JPanel();
        panel18.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel17.add(panel18, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel19 = new JPanel();
        panel19.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel18.add(panel19, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Chat name:");
        panel19.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel20 = new JPanel();
        panel20.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel18.add(panel20, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Members (sep = ';'):");
        panel20.add(label7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel21 = new JPanel();
        panel21.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel17.add(panel21, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel22 = new JPanel();
        panel22.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel21.add(panel22, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        createChatName = new JTextField();
        createChatName.setEnabled(false);
        panel22.add(createChatName, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel23 = new JPanel();
        panel23.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel21.add(panel23, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        createChatMembers = new JTextField();
        createChatMembers.setEnabled(false);
        panel23.add(createChatMembers, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel24 = new JPanel();
        panel24.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel15.add(panel24, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setEnabled(true);
        label8.setText("Create chat or add chat members");
        panel24.add(label8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator2 = new JSeparator();
        panel10.add(separator2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
