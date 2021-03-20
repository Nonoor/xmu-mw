package cn.edu.xmu.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.ByteArrayInputStream;

@FXMLController
public class sceneController {
    @FXML
    private TextField sendToTextField;

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
    private String codeUser;
    private MessageProducer messageProducer;


    @FXML
    void connect(ActionEvent event) {
        try {
            codeUser = usernameTextField.getText();
            String host = "localhost";
            int port = 61616;
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://" + host + ":" + port);
            Connection connection = connectionFactory.createConnection();
            connection.start();
            session=connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
            Destination destination=session.createTopic("enset.chat");
            MessageConsumer consumer=session.createConsumer(destination,"code='"+codeUser+"'");
            messageProducer=session.createProducer(destination);
            messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            connectButton.setDisable(true);
            usernameTextField.setDisable(true);
            consumer.setMessageListener(message ->  {
                try {
                    if(message instanceof TextMessage){
                        TextMessage textMessage=(TextMessage)message;
                        chatRecord.appendText(textMessage.getText()+'\n');
                        messageList.getItems().add(0,textMessage.getText());;
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
        try{
            TextMessage textMessage=session.createTextMessage();
            textMessage.setText(message.getText());
            textMessage.setStringProperty("code",sendToTextField.getText());
            messageProducer.send(textMessage);
            message.clear();
//                observableListMessages.add(codeUser + ":" + textMessage.getText());
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
