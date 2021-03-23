package cn.edu.xmu.controller;

import cn.edu.xmu.constant.ChatType;
import de.felixroske.jfxsupport.FXMLController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.*;

/**
 * @author zhibin lan
 * @date 2021-03-20
 */
@FXMLController
public class SceneController {
    @FXML
    private Label fileNameLabel;

    @FXML
    private TextField sendToUserTextField;

    @FXML
    private TextField sendToGroupTextField;

    @FXML
    private TextArea message;

    @FXML
    private TextArea chatRecord;

    @FXML
    private Button connectButton;

    @FXML
    private TextField usernameTextField;

    @FXML
    private Button sendButton;

    @FXML
    private ListView<String> messageList;

    private Session session;
    private String username;
    private MessageProducer uMessageProducer;
    private MessageProducer gMessageProducer;
    private MessageProducer fMessageProducer;
    private ChatType curType;
    private File file;

    @FXML
    void connect(ActionEvent event) {
        try {
            username = usernameTextField.getText();
            String host = "localhost";
            int port = 61616;
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://" + host + ":" + port);
            Connection connection = connectionFactory.createConnection();
            connection.start();
            session=connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
            Destination uDestination = session.createQueue("user.chat");
            //默认为用户联系
            curType = ChatType.USER;
            MessageConsumer uConsumer=session.createConsumer(uDestination,ChatType.USER.getType() + "='"+ username +"'");
            uMessageProducer =session.createProducer(uDestination);
            uMessageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            connectButton.setDisable(true);
            usernameTextField.setDisable(true);
            messageList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>(){
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
                    if(!newValue.equals(oldValue)){
                        if(newValue.contains(ChatType.USER.getType())){
                            curType = ChatType.USER;
                        }
                        else if(newValue.contains(ChatType.GROUP.getType())){
                            curType = ChatType.GROUP;
                        }
                    }
                }
            });

            uConsumer.setMessageListener(message ->  {
                try {
                    if(message instanceof TextMessage){
                        TextMessage textMessage=(TextMessage)message;
                        String fromUser = textMessage.getStringProperty("from");
                        String type = textMessage.getStringProperty("type");
                        chatRecord.appendText(fromUser + ":\n");
                        chatRecord.appendText(textMessage.getText()+'\n');
                        if(!messageList.getItems().contains(type + fromUser)){
                            messageList.getItems().add(0,type + fromUser);
                        }
                    }
                    else if(message instanceof BytesMessage){
                        BytesMessage bytesMessage=(BytesMessage)message;
                        String name = bytesMessage.getStringProperty("name");
                        byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
                        bytesMessage.readBytes(bytes);
                        File file = new File(".\\"+name);
                        if(!file.exists()){
                            file.createNewFile();
                        }
                        OutputStream os = new FileOutputStream(file);
                        os.write(bytes);
                        os.close();
                        chatRecord.appendText("file: " + name);
                    }
                    else{

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    void send(ActionEvent event) {
        //私聊
        if(curType.equals(ChatType.USER)){
            try{
                System.out.println("user");
                TextMessage textMessage=session.createTextMessage();
                textMessage.setText(message.getText());

                String desUser = messageList.getSelectionModel().getSelectedItem().substring(6);
                textMessage.setStringProperty(curType.getType(), desUser);
                textMessage.setStringProperty("from",username);
                textMessage.setStringProperty("type",curType.getType()+": ");
                uMessageProducer.send(textMessage);
                message.clear();

                chatRecord.appendText(username + ":\n");
                chatRecord.appendText(textMessage.getText()+'\n');
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        //群聊
        else {
            try{
                TextMessage textMessage=session.createTextMessage();
                textMessage.setText(message.getText());

                String desgroup= messageList.getSelectionModel().getSelectedItem().substring(7);
                textMessage.setStringProperty(curType.getType(), desgroup);
                textMessage.setStringProperty("from",username);
                textMessage.setStringProperty("group",desgroup);
                textMessage.setStringProperty("type",curType.getType()+": ");
                gMessageProducer.send(textMessage);
                message.clear();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    @FXML
    void addConnectUser(ActionEvent event) {
        if(!messageList.getItems().contains(sendToUserTextField.getText()) && !sendToUserTextField.getText().equals(username)){
            messageList.getItems().add(0, ChatType.USER.getType() +": " + sendToUserTextField.getText());
        }
    }

    @FXML
    void addConnectGroup(ActionEvent event) {
        if(!messageList.getItems().contains(sendToGroupTextField.getText())){
            messageList.getItems().add(0,ChatType.GROUP.getType() +": " + sendToGroupTextField.getText());
            try {
                Destination gDestination = session.createTopic("group.chat");
                MessageConsumer gConsumer=session.createConsumer(gDestination,ChatType.GROUP.getType() + "='"+ sendToGroupTextField.getText() +"'");
                gMessageProducer =session.createProducer(gDestination);
                gMessageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                gConsumer.setMessageListener(message ->  {
                    try {
                        if(message instanceof TextMessage){
                            TextMessage textMessage=(TextMessage)message;
                            String fromUser = textMessage.getStringProperty("from");
                            String fromGroup = textMessage.getStringProperty("group");
                            String type = textMessage.getStringProperty("type");
                            chatRecord.appendText(fromUser + ":\n");
                            chatRecord.appendText(textMessage.getText()+'\n');
                            if(!messageList.getItems().contains(type + fromGroup)){
                                messageList.getItems().add(0,type + fromUser);
                            }
                        }
                        else if(message instanceof BytesMessage){
                            BytesMessage bytesMessage=(BytesMessage)message;
                            String name = bytesMessage.getStringProperty("name");
                            byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
                            bytesMessage.readBytes(bytes);
                            File file = new File(".\\"+name);
                            if(!file.exists()){
                                file.createNewFile();
                            }
                            OutputStream os = new FileOutputStream(file);
                            os.write(bytes);
                            os.close();
                            chatRecord.appendText("file: " + name);
                        }
                        else{

                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

    }

    @FXML
    void sendFile(ActionEvent event) throws IOException, JMSException {
        FileInputStream is = new FileInputStream(file);
        byte[] fileBytes = new byte[is.available()];
        is.read(fileBytes);
        BytesMessage bytesMessage = session.createBytesMessage();
        bytesMessage.writeBytes(fileBytes);
        bytesMessage.setStringProperty("name",file.getName());
        bytesMessage.setStringProperty("type",curType.getType()+": ");
        if(curType == ChatType.USER) {
            String desUser = messageList.getSelectionModel().getSelectedItem().substring(6);
            bytesMessage.setStringProperty(curType.getType(), desUser);
            uMessageProducer.send(bytesMessage);
        }
        else{
            try {
                String desgroup= messageList.getSelectionModel().getSelectedItem().substring(7);
                bytesMessage.setStringProperty(curType.getType(), desgroup);
                gMessageProducer.send(bytesMessage);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @FXML
    void selectFile(ActionEvent event) {
        file = null;
        fileNameLabel.setText("");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开文件");
        file = fileChooser.showOpenDialog(null);
        fileNameLabel.setText(file.getName());
    }
}
