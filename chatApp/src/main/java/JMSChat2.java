import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.*;
import java.net.MalformedURLException;

public class JMSChat2 extends Application {

    private Session session;
    private String codeUser;
    private MessageProducer messageProducer;

    @Override
    public void start(Stage primaryStage) throws Exception{
        BorderPane borderPane=new BorderPane();
        HBox hBox=new HBox();
        hBox.setPadding(new Insets(10));
        hBox.setSpacing(10);
        hBox.setBackground(new Background(new BackgroundFill(Color.ORANGE, CornerRadii.EMPTY, Insets.EMPTY)));

        ObservableList<String> observableListMessages=FXCollections.observableArrayList();

        Label labelCode=new Label("Code :");
        labelCode.setPadding(new Insets(5));
        TextField textFieldCode=new TextField("C1");
        textFieldCode.setPromptText("Code");

        Label labelHost=new Label("Host :");
        labelHost.setPadding(new Insets(5));
        TextField textFieldHost=new TextField("localhost");
        textFieldHost.setPromptText("Host");

        Label labelPort=new Label("Port :");
        labelPort.setPadding(new Insets(5));
        TextField textFieldPort=new TextField("61616");
        textFieldPort.setPromptText("Port");

        Button buttonConnect=new Button("Connecter");


        hBox.getChildren().addAll(labelCode,textFieldCode,labelHost,textFieldHost,labelPort,textFieldPort,buttonConnect);

        borderPane.setTop(hBox);

        VBox vBox = new VBox();
        GridPane gridPane=new GridPane();
        HBox hBox2=new HBox();
        vBox.getChildren().addAll(gridPane, hBox2);
        borderPane.setCenter(vBox);
        gridPane.setHgap(10); gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10));
        gridPane.add(new Label("To :"),0,0);
        TextField textdest=new TextField("C1");
        textdest.setPrefWidth(500);
        gridPane.add(textdest,1,0);
        gridPane.add(new Label("Message :"),0,1);
        TextArea textmessage= new TextArea();
        textmessage.setPrefRowCount(2);
        textmessage.setPrefWidth(500);
        gridPane.add(textmessage,1,1);
        Button send =new Button("send");
        gridPane.add(send,2,1);
        gridPane.add(new Label("Image :"),0,2);
        File f=new File("images");
        ObservableList<String> observableListImages= FXCollections.observableArrayList(f.list());
        ComboBox comboBoxImages= new ComboBox<String>(observableListImages);
        comboBoxImages.getSelectionModel().select(0);
        gridPane.add(comboBoxImages,1,2);
        Button sendImage=new Button("send image");
        gridPane.add(sendImage,2,2);

        ListView<String> stringListView=new ListView<>(observableListMessages);

        File file=new File("images/"+comboBoxImages.getSelectionModel().getSelectedItem());
        Image image=new Image(file.toURL().toString());
        ImageView imageView=new ImageView(image);
        imageView.setFitHeight(280); imageView.setFitWidth(600);
        hBox2.getChildren().addAll(stringListView, imageView);
        hBox2.setPadding(new Insets(10));
        hBox2.setSpacing(10);

        comboBoxImages.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                File file1=new File("images/"+newValue);
                Image image1= null;
                try {
                    image1 = new Image(file1.toURL().toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                imageView.setImage(image1);
            }
        });

        send.setOnAction(e->{
            try{
                TextMessage textMessage=session.createTextMessage();
                textMessage.setText(textmessage.getText());
                textMessage.setStringProperty("code",textdest.getText());
                messageProducer.send(textMessage);
//                observableListMessages.add(codeUser + ":" + textMessage.getText());
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });

        sendImage.setOnAction(e->{
            try {
                StreamMessage streamMessage=session.createStreamMessage();
                streamMessage.setStringProperty("code",textdest.getText());
                streamMessage.setStringProperty("from", codeUser);
                File file2=new File("images/"+comboBoxImages.getSelectionModel().getSelectedItem());
                FileInputStream fis=new FileInputStream(file2);
                byte[] data=new byte[(int)file2.length()];
                fis.read(data);
                streamMessage.writeString((String) comboBoxImages.getSelectionModel().getSelectedItem());
                streamMessage.writeInt(data.length);
                streamMessage.writeBytes(data);
                messageProducer.send(streamMessage);
            } catch (JMSException | FileNotFoundException jmsException) {
                jmsException.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        });

        buttonConnect.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    codeUser = textFieldCode.getText();
                    String host = textFieldHost.getText();
                    int port = Integer.parseInt(textFieldPort.getText());
                    ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://" + host + ":" + port);
                    Connection connection = connectionFactory.createConnection();
                    connection.start();
                    session=connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
                    Destination destination=session.createTopic("enset.chat");
                    MessageConsumer consumer=session.createConsumer(destination,"code='"+codeUser+"'");
                    messageProducer=session.createProducer(destination);
                    messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                    consumer.setMessageListener(message ->  {
                        try {
                            if(message instanceof TextMessage){
                                TextMessage textMessage=(TextMessage) message;
                                observableListMessages.add(textMessage.getText());
                            }
                            else{
                                StreamMessage streamMessage=(StreamMessage) message;
                                String nomPhoto=streamMessage.readString();
                                System.out.println(nomPhoto);
                                observableListMessages.add("réception d'image :"+nomPhoto);
                                int size=streamMessage.readInt();
                                byte[] data=new byte[size];
                                streamMessage.readBytes(data);
                                ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(data);
                                Image image=new Image(byteArrayInputStream);
                                imageView.setImage(image);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
                hBox.setDisable(true);
            }
        });

        Scene scene=new Scene(borderPane,900,500);
        primaryStage.setTitle("JMS CHAT");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
