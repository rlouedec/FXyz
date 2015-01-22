package org.fxyz.samples;

import java.util.concurrent.atomic.AtomicInteger;
import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.CullFace;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import org.fxyz.FXyzSample;
import org.fxyz.geometry.DensityFunction;
import org.fxyz.geometry.Point3D;
import org.fxyz.shapes.primitives.SpringMesh;
import org.fxyz.shapes.primitives.helper.TriangleMeshHelper.SectionType;
import org.fxyz.utils.CameraTransformer;

/**
 *
 * @author jpereda
 */
public class Springs extends FXyzSample {

    @Override
    public Node getSample(){
        PerspectiveCamera camera;
        final double sceneWidth = 800;
        final double sceneHeight = 600;
        final CameraTransformer cameraTransform = new CameraTransformer();

        SpringMesh spring;
        Rotate rotateY;
        DensityFunction<Point3D> dens = p -> (double) p.x;
        long lastEffect;

        Group sceneRoot = new Group();
        SubScene scene = new SubScene(sceneRoot, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.BLACK);
        camera = new PerspectiveCamera(true);

        //setup camera transform for rotational support
        cameraTransform.setTranslate(0, 0, 0);
        cameraTransform.getChildren().add(camera);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-500);
        cameraTransform.ry.setAngle(-45.0);
        cameraTransform.rx.setAngle(-10.0);
        //add a Point Light for better viewing of the grid coordinate system
        PointLight light = new PointLight(Color.WHITE);
        cameraTransform.getChildren().add(light);
//        cameraTransform.getChildren().add(new AmbientLight(Color.WHITE));
        light.setTranslateX(camera.getTranslateX());
        light.setTranslateY(camera.getTranslateY());
        light.setTranslateZ(camera.getTranslateZ());
        scene.setCamera(camera);

        rotateY = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
        Group group = new Group();
        group.getChildren().add(cameraTransform);

        spring = new SpringMesh(50d, 10d, 20d, 2 * 20d * 2d * Math.PI,
                1000, 60, 0, 0);
//        spring.setDrawMode(DrawMode.LINE);
        spring.setCullFace(CullFace.NONE);
        spring.setSectionType(SectionType.TRIANGLE);

//    // NONE
//        spring.setTextureModeNone(Color.ROYALBLUE);
        // IMAGE
//        spring.setTextureModeImage(getClass().getResource("res/LaminateSteel.jpg").toExternalForm());
        // PATTERN
//       spring.setTextureModePattern(5d);
        // FUNCTION
//        spring.setTextureModeVertices1D(256*256,t->t);
        // DENSITY
        spring.setTextureModeVertices3D(256 * 256, p -> (double) p.magnitude());
    // FACES
//        spring.setTextureModeFaces(256*256);

        spring.getTransforms().addAll(new Rotate(0, Rotate.X_AXIS), rotateY);

        group.getChildren().add(spring);

        sceneRoot.getChildren().addAll(group);

        //First person shooter keyboard movement 
        scene.setOnKeyPressed(event -> {
            double change = 10.0;
            //Add shift modifier to simulate "Running Speed"
            if (event.isShiftDown()) {
                change = 50.0;
            }
            //What key did the user press?
            KeyCode keycode = event.getCode();
            //Step 2c: Add Zoom controls
            if (keycode == KeyCode.W) {
                camera.setTranslateZ(camera.getTranslateZ() + change);
            }
            if (keycode == KeyCode.S) {
                camera.setTranslateZ(camera.getTranslateZ() - change);
            }
            //Step 2d:  Add Strafe controls
            if (keycode == KeyCode.A) {
                camera.setTranslateX(camera.getTranslateX() - change);
            }
            if (keycode == KeyCode.D) {
                camera.setTranslateX(camera.getTranslateX() + change);
            }
        });

        scene.setOnMousePressed((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        scene.setOnMouseDragged((MouseEvent me) -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);

            double modifier = 10.0;
            double modifierFactor = 0.1;

            if (me.isControlDown()) {
                modifier = 0.1;
            }
            if (me.isShiftDown()) {
                modifier = 50.0;
            }
            if (me.isPrimaryButtonDown()) {
                cameraTransform.ry.setAngle(((cameraTransform.ry.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);  // +
                cameraTransform.rx.setAngle(((cameraTransform.rx.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);  // -
            } else if (me.isSecondaryButtonDown()) {
                double z = camera.getTranslateZ();
                double newZ = z + mouseDeltaX * modifierFactor * modifier;
                camera.setTranslateZ(newZ);
            } else if (me.isMiddleButtonDown()) {
                cameraTransform.t.setX(cameraTransform.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3);  // -
                cameraTransform.t.setY(cameraTransform.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3);  // -
            }
        });

        lastEffect = System.nanoTime();
        AtomicInteger count = new AtomicInteger();
        AnimationTimer timerEffect = new AnimationTimer() {

            @Override
            public void handle(long now) {
                if (now > lastEffect + 500_000_000l) {
//                    dens = p->(float)(p.x*Math.cos(count.get()%100d*2d*Math.PI/50d)+p.y*Math.sin(count.get()%100d*2d*Math.PI/50d));
//                    spring.setDensity(dens);
//                    spring.setPitch(20+5*(count.get()%10));

//                    if(count.get()%100<50){
//                        spring.setDrawMode(DrawMode.LINE);
//                    } else {
//                        spring.setDrawMode(DrawMode.FILL);
//                    }
                    spring.setLength(100 + 20 * (count.get() % 10));
//                    spring.setPatternScale(1d+(count.get()%10)*2d);
//                    spring.setSectionType(SectionType.values()[count.get()%SectionType.values().length]);

//                    spring.setColors((int)Math.pow(2,count.get()%16));
//                    spring.setMeanRadius(50+10*(count.get()%10));
                    count.getAndIncrement();
                    //lastEffect = now;
                }
            }
        };

        //OBJWriter writer=new OBJWriter((TriangleMesh) spring.getMesh(),"spring");
//        writer.setMaterialColor(Color.BROWN);
        //writer.setTextureColors(256*256);
        //writer.exportMesh();
//        timerEffect.start();
        
        StackPane sp = new StackPane();
        sp.setPrefSize(sceneWidth, sceneHeight);
        sp.setMaxSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
        sp.setMinSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
        sp.setBackground(Background.EMPTY);
        sp.getChildren().add(scene);
        sp.setPickOnBounds(false);
        
        scene.widthProperty().bind(sp.widthProperty());
        scene.heightProperty().bind(sp.heightProperty());
        
        return (sp);
    }

    @Override
    public String getSampleName() {
        return getClass().getSimpleName().concat(" Sample");
    }

    @Override
    public Node getPanel(Stage stage) {
        
        return getSample();
    }

    @Override
    public String getJavaDocURL() {
        return "";
    }
}
