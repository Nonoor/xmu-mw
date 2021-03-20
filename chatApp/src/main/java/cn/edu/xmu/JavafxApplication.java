package cn.edu.xmu;

import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import cn.edu.xmu.view.InitSceneView;


@SpringBootApplication
public class JavafxApplication extends AbstractJavaFxApplicationSupport {

    public static void main(String[] args) {
        //改成launch方法,
        launch(JavafxApplication.class, InitSceneView.class,args);
    }

}
