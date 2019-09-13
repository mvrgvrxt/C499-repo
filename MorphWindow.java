import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;

public class MorphWindow extends JFrame implements MouseListener, MouseMotionListener{

    public static final int MIN_INTENSITY = -100;
    public static final int MAX_INTENSITY = 200;
    public static final int DEFAULT_INTENSITY = 0;
    public static final int MIN_SECS = 1;
    public static final int MAX_SECS = 20;
    public static final int DEFAULT_SECS = 6;
    public static final int MIN_FPS = 2;
    public static final int MAX_FPS = 30;
    public static final int DEFAULT_FPS = 24;
    public static final int IMG_WIDTH = 600;
    public static final int IMG_HEIGHT = 600;
    public static final int MIN_DIM = 5;
    public static final int GRID_DIM = 10;
    public static final int MAX_DIM = 20;

    private MyImageObj imageInitial, imageFinal;
    private PreviewWindow preview;
    private JSlider initialIntensitySlider, finalIntensitySlider;   // set brightness//intensity of images
    private JSlider gridSlider;     // choose dimension of control nodes
    private JSlider secondsSlider;  // choose number of seconds for animation
    private JSlider fpsSlider;      // choose number of frames per second
    private JLabel initialIntensityLabel, finalIntensityLabel, gridLabel, secondsLabel, fpsLabel;
    private JButton previewButton, morphButton;
    private int framesPerSecond, seconds;
    private int gridSize;           // number of movable points in a row of ctrl pts
    private int[] selectedPoint;    // currently selected point
    private boolean imageInitialLoaded = false;
    private boolean imageFinalLoaded = false;
    boolean isDragging = false;

    public MorphWindow() {
        super("Morph");

        // Initialize variables
        framesPerSecond = DEFAULT_FPS;
        seconds = DEFAULT_SECS;
        gridSize = GRID_DIM;

        // Instantiate intensity slider for initial image with constant parameters, define behavior
        initialIntensitySlider = new JSlider(MIN_INTENSITY, MAX_INTENSITY, DEFAULT_INTENSITY);
        initialIntensitySlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // If image has been loaded
                if (imageInitialLoaded) {
                    // Get image without any intensity adjustments
                    BufferedImage I = imageInitial.getOriginalImg();

                    // Create copy of image
                    BufferedImage F;
                    Graphics2D g;
                    F = new BufferedImage(I.getWidth(), I.getHeight(), I.getType());
                    g = F.createGraphics();
                    g.drawImage(I, 0, 0, null);
                    g.dispose();

                    // Define scale depending on current value of slider
                    float f = 1.0f + ((float)initialIntensitySlider.getValue() / 100);

                    // Change intensity with scale f
                    RescaleOp r = new RescaleOp(f, 0, null);
                    r.filter(I, F);

                    // Set adjusted image
                    imageInitial.setFilteredImg(F);
                }
            }
        });

        // Instantiate intensity slider for final image with constant parameters, define behavior
        finalIntensitySlider = new JSlider(MIN_INTENSITY, MAX_INTENSITY, DEFAULT_INTENSITY);
        finalIntensitySlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (imageFinalLoaded) {
                    // Get image without any intensity adjustments
                    BufferedImage I = imageFinal.getOriginalImg();

                    // Create copy of image
                    BufferedImage F;
                    Graphics2D g;
                    F = new BufferedImage(I.getWidth(), I.getHeight(), I.getType());
                    g = F.createGraphics();
                    g.drawImage(I, 0, 0, null);
                    g.dispose();

                    // Define scale depending on current value of slider
                    float f = 1.0f + ((float)finalIntensitySlider.getValue() / 100);

                    // Change intensity with scale f
                    RescaleOp r = new RescaleOp(f, 0, null);
                    r.filter(I, F);

                    // Set adjusted image
                    imageFinal.setFilteredImg(F);
                }
            }
        });

        // Instantiate gridSlider with constant parameters, define behavior
        gridSlider = new JSlider(MIN_DIM, MAX_DIM, GRID_DIM);
        gridSlider.setMajorTickSpacing(5);
        gridSlider.setMinorTickSpacing(1);
        gridSlider.setPaintLabels(true);
        gridSlider.setPaintTicks(true);
        gridSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // Get current value
                gridSize = gridSlider.getValue();

                // Set grid resolutions to gridSize
                imageInitial.setGridSize(gridSize);
                imageFinal.setGridSize(gridSize);

                // Update label
                gridLabel.setText("Grid resolution: " + gridSize);
            }
        });

        // Instantiate secondsSlider with constant parameters, define behavior
        secondsSlider = new JSlider(MIN_SECS, MAX_SECS, DEFAULT_SECS);
        secondsSlider.setMajorTickSpacing(5);
        secondsSlider.setMinorTickSpacing(1);
        secondsSlider.setPaintTicks(true);
        secondsSlider.setPaintLabels(true);
        secondsSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // Update seconds var
                seconds = secondsSlider.getValue();
                // Update label
                secondsLabel.setText("Seconds: " + seconds);
                // If preview window has been instantiated, update its seconds var
                if (preview != null) {
                    preview.setSeconds(seconds);
                }
            }
        });

        // Instantiate fpsSlider with constant parameters, define behavior
        fpsSlider = new JSlider(MIN_FPS, MAX_FPS, DEFAULT_FPS);
        fpsSlider.setMajorTickSpacing(6);
        fpsSlider.setMinorTickSpacing(1);
        fpsSlider.setPaintTicks(true);
        fpsSlider.setPaintLabels(true);
        fpsSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // Update fps var
                framesPerSecond = fpsSlider.getValue();
                // Update label
                fpsLabel.setText("Frames per second: " + framesPerSecond);
                // If preview window has been instantiated, update its fps var
                if (preview != null) {
                    preview.setFramesPerSecond(seconds);
                }
            }
        });

        // Instantiate intensity labels
        initialIntensityLabel = new JLabel("Intensity");
        finalIntensityLabel = new JLabel("Intensity");

        // Instantiate slider labels
        gridLabel = new JLabel("Grid resolution: ");
        secondsLabel = new JLabel("Seconds: " + seconds);
        fpsLabel = new JLabel("Frames per second: " + framesPerSecond);

        // Instantiate and define behavior of previewButton
        previewButton = new JButton("Preview");
        previewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Close preview window if already open
                if (preview != null) {
                    preview.dispatchEvent(new WindowEvent(preview, WindowEvent.WINDOW_CLOSING));
                }

                // Instantiate new preview window with isMorph false
                preview = new PreviewWindow(imageInitial, imageFinal, framesPerSecond, seconds, false);

                // Start animation timer
                preview.startTimer();
            }
        });

        // Instantiate and define behavior of morphButton
        morphButton = new JButton("Morph");
        morphButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // If both images are loaded
                if (imageInitialLoaded && imageFinalLoaded) {
                    // Update label
                    morphButton.setText("Morphing...");
                    repaint();

                    // Disable button
                    morphButton.setEnabled(false);

                    // Compute and render morph frames
                    generateMorph();

                    // Re-enable button
                    morphButton.setEnabled(true);

                    // Update label
                    morphButton.setText("Morph");
                    repaint();

                    // Display morph
                    // Close preview window if already open
                    if (preview != null) {
                        preview.dispatchEvent(new WindowEvent(preview, WindowEvent.WINDOW_CLOSING));
                    }

                    // Instantiate new preview window with isMorph true
                    preview = new PreviewWindow(imageInitial, imageFinal, framesPerSecond, seconds, true);

                    // Start animation timer
                    preview.startTimer();
                }
            }
        });

        // Instantiate MyImageObjs
        imageInitial = new MyImageObj(IMG_WIDTH, IMG_HEIGHT, gridSize);
        imageFinal = new MyImageObj(IMG_WIDTH, IMG_HEIGHT, gridSize);

        // Use this frame to define mouse behavior on labels
        imageInitial.addMouseListener(this);
        imageInitial.addMouseMotionListener(this);
        imageFinal.addMouseListener(this);
        imageFinal.addMouseMotionListener(this);

        // Instantiate file menu
        final JFileChooser fc = new JFileChooser(".");
        JMenuBar bar = new JMenuBar();
        setJMenuBar(bar);
        JMenu fileMenu = new JMenu ("File");
        JMenuItem fileInitialOpen = new JMenuItem ("Open First Image");
        JMenuItem fileFinalOpen = new JMenuItem ("Open Second Image");
        JMenuItem fileExit = new JMenuItem ("Exit");

        // Define file opening behavior
        fileInitialOpen.addActionListener(
                new ActionListener () {
                    public void actionPerformed (ActionEvent e) {
                        int returnVal = fc.showOpenDialog(MorphWindow.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fc.getSelectedFile();
                            try {
                                imageInitial.setImage(ImageIO.read(file));
                            } catch (IOException e1){};
                            imageInitialLoaded = true;
                        }
                        //MorphWindow.this.pack();
                    }
                }
        );
        fileFinalOpen.addActionListener(
                new ActionListener () {
                    public void actionPerformed (ActionEvent e) {
                        int returnVal = fc.showOpenDialog(MorphWindow.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fc.getSelectedFile();
                            try {
                                imageFinal.setImage(ImageIO.read(file));
                            } catch (IOException e1){};
                            imageFinalLoaded = true;
                        }
                        //MorphWindow.this.pack();
                    }
                }
        );
        fileExit.addActionListener(
                new ActionListener() {
                    public void actionPerformed (ActionEvent e) {
                        System.exit(0);
                    }
                }
        );

        // Put menu components together
        fileMenu.add(fileInitialOpen);
        fileMenu.add(fileFinalOpen);
        fileMenu.add(fileExit);
        bar.add(fileMenu);

        Container c = getContentPane();

        // Add MyImgObjs to frame
        c.add(imageInitial, BorderLayout.WEST);
        c.add(imageFinal, BorderLayout.EAST);

        // Create panels for intensity sliders and labels
        JPanel initialIntensityPanel, finalIntensityPanel;
        initialIntensityPanel = new JPanel(new GridLayout(1, 2));
        initialIntensityPanel.add(initialIntensityLabel);
        initialIntensityPanel.add(initialIntensitySlider);
        finalIntensityPanel = new JPanel(new GridLayout(1, 2));
        finalIntensityPanel.add(finalIntensityLabel);
        finalIntensityPanel.add(finalIntensitySlider);

        // Add control components to control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(5, 2));
        controlPanel.add(initialIntensityPanel);
        controlPanel.add(finalIntensityPanel);
        controlPanel.add(gridLabel);
        controlPanel.add(gridSlider);
        controlPanel.add(secondsLabel);
        controlPanel.add(secondsSlider);
        controlPanel.add(fpsLabel);
        controlPanel.add(fpsSlider);
        controlPanel.add(previewButton);
        controlPanel.add(morphButton);
        c.add(controlPanel, BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }

    public void generateMorph() {

        // I, F are input images
        // imageInit and imageFin are copies of these
        // imageInitTween is a generated warped frame of imageInit
        // imageFinTween is a generated warped frame of imageFin
        BufferedImage I, F, imageInit, imageFin, imageInitTween, imageFinTween;

        // Copy I, F into imageInit, imageFin
        Graphics2D g1, g2;
        I = imageInitial.getImage();
        F = imageFinal.getImage();
        imageInit = new BufferedImage(I.getWidth(), I.getHeight(), I.getType());
        imageInitTween = new BufferedImage(I.getWidth(), I.getHeight(), I.getType());
        imageFin = new BufferedImage(F.getWidth(), F.getHeight(), F.getType());
        imageFinTween = new BufferedImage(F.getWidth(), F.getHeight(), F.getType());
        g1 = imageInit.createGraphics();
        g2 = imageFin.createGraphics();
        g1.drawImage(I, 0, 0, null);
        g2.drawImage(F, 0, 0, null);
        g1.dispose();
        g2.dispose();

        // Calculate factors for scaling control point grid to image sizes
        double initialWidthFactor, initialHeightFactor, finalWidthFactor, finalHeightFactor;
        initialWidthFactor = (double)I.getWidth()/IMG_WIDTH;
        initialHeightFactor = (double)I.getHeight()/IMG_HEIGHT;
        finalWidthFactor = (double)F.getWidth()/IMG_WIDTH;
        finalHeightFactor = (double)F.getHeight()/IMG_HEIGHT;

        // Get control point positions from images
        int[][][] initPoints = imageInitial.getControlPoints();
        int[][][] finalPoints = imageFinal.getControlPoints();

        // Define arrays of triangles for initial and final positions for both images
        Triangle[][][] initTriangles1 = new Triangle[gridSize+1][gridSize+1][2];
        Triangle[][][] finalTriangles1 = new Triangle[gridSize+1][gridSize+1][2];
        Triangle[][][] initTriangles2 = new Triangle[gridSize+1][gridSize+1][2];
        Triangle[][][] finalTriangles2 = new Triangle[gridSize+1][gridSize+1][2];

        // Loop over upper left corners of grid triangles
        for (int i = 0; i < gridSize + 1; i++) {
            for (int j = 0; j < gridSize + 1; j++) {
                // Define triangles by scaling control points to image size

                initTriangles1[i][j][0] = new Triangle(
                        (int)(initPoints[i][j][0] * initialWidthFactor),
                        (int)(initPoints[i][j][1] * initialHeightFactor),
                        (int)(initPoints[i + 1][j][0] * initialWidthFactor),
                        (int)(initPoints[i + 1][j][1] * initialHeightFactor),
                        (int)(initPoints[i + 1][j + 1][0] * initialWidthFactor),
                        (int)(initPoints[i + 1][j + 1][1] * initialHeightFactor));
                initTriangles1[i][j][1] = new Triangle(
                        (int)(initPoints[i][j][0] * initialWidthFactor),
                        (int)(initPoints[i][j][1] * initialHeightFactor),
                        (int)(initPoints[i][j + 1][0] * initialWidthFactor),
                        (int)(initPoints[i][j + 1][1] * initialHeightFactor),
                        (int)(initPoints[i + 1][j + 1][0] * initialWidthFactor),
                        (int)(initPoints[i + 1][j + 1][1] * initialHeightFactor));
                finalTriangles1[i][j][0] = new Triangle(
                        (int)(finalPoints[i][j][0] * initialWidthFactor),
                        (int)(finalPoints[i][j][1] * initialHeightFactor),
                        (int)(finalPoints[i + 1][j][0] * initialWidthFactor),
                        (int)(finalPoints[i + 1][j][1] * initialHeightFactor),
                        (int)(finalPoints[i + 1][j + 1][0] * initialWidthFactor),
                        (int)(finalPoints[i + 1][j + 1][1] * initialHeightFactor));
                finalTriangles1[i][j][1] = new Triangle(
                        (int)(finalPoints[i][j][0] * initialWidthFactor),
                        (int)(finalPoints[i][j][1] * initialHeightFactor),
                        (int)(finalPoints[i][j + 1][0] * initialWidthFactor),
                        (int)(finalPoints[i][j + 1][1] * initialHeightFactor),
                        (int)(finalPoints[i + 1][j + 1][0] * initialWidthFactor),
                        (int)(finalPoints[i + 1][j + 1][1] * initialHeightFactor));

                initTriangles2[i][j][0] = new Triangle(
                        (int)(initPoints[i][j][0] * finalWidthFactor),
                        (int)(initPoints[i][j][1] * finalHeightFactor),
                        (int)(initPoints[i + 1][j][0] * finalWidthFactor),
                        (int)(initPoints[i + 1][j][1] * finalHeightFactor),
                        (int)(initPoints[i + 1][j + 1][0] * finalWidthFactor),
                        (int)(initPoints[i + 1][j + 1][1] * finalHeightFactor));
                initTriangles2[i][j][1] = new Triangle(
                        (int)(initPoints[i][j][0] * finalWidthFactor),
                        (int)(initPoints[i][j][1] * finalHeightFactor),
                        (int)(initPoints[i][j + 1][0] * finalWidthFactor),
                        (int)(initPoints[i][j + 1][1] * finalHeightFactor),
                        (int)(initPoints[i + 1][j + 1][0] * finalWidthFactor),
                        (int)(initPoints[i + 1][j + 1][1] * finalHeightFactor));
                finalTriangles2[i][j][0] = new Triangle(
                        (int)(finalPoints[i][j][0] * finalWidthFactor),
                        (int)(finalPoints[i][j][1] * finalHeightFactor),
                        (int)(finalPoints[i + 1][j][0] * finalWidthFactor),
                        (int)(finalPoints[i + 1][j][1] * finalHeightFactor),
                        (int)(finalPoints[i + 1][j + 1][0] * finalWidthFactor),
                        (int)(finalPoints[i + 1][j + 1][1] * finalHeightFactor));
                finalTriangles2[i][j][1] = new Triangle(
                        (int)(finalPoints[i][j][0] * finalWidthFactor),
                        (int)(finalPoints[i][j][1] * finalHeightFactor),
                        (int)(finalPoints[i][j + 1][0] * finalWidthFactor),
                        (int)(finalPoints[i][j + 1][1] * finalHeightFactor),
                        (int)(finalPoints[i + 1][j + 1][0] * finalWidthFactor),
                        (int)(finalPoints[i + 1][j + 1][1] * finalHeightFactor));
            }
        }

        // Loop over number of frames to be generated
        for (int i = 0; i < seconds*framesPerSecond; i++) {
            // Morph i-th frame of initial image
            morphFrame(initTriangles1, finalTriangles1, i, imageInit, imageInitTween);

            // Morph corresponding frame of final image
            morphFrame(finalTriangles2, initTriangles2, seconds*framesPerSecond-i-1, imageFin, imageFinTween);

            // Composite frames together in place on imageInitTween
            Graphics2D g = imageInitTween.createGraphics();
            float p = (float) i / (seconds * framesPerSecond - 1);
            g.setComposite(AlphaComposite.SrcOver.derive(p));
            g.drawImage(imageFinTween, 0, 0, imageInitTween.getWidth(), imageInitTween.getHeight(), null);
            g.dispose();

            // Write frame to JPEG
            writeJPEG(imageInitTween, "renders/" + Integer.toString(i));
        }
    }

    private void morphFrame(Triangle[][][] initTriangles, Triangle[][][] finalTriangles, int index, BufferedImage init, BufferedImage tween) {
        // Loop over triangles
        for (int i = 0; i < gridSize + 1; i++) {
            for (int j = 0; j < gridSize + 1; j++) {
                for (int k = 0; k < 2; k++) {
                    // Define points for current triangle's current location
                    double[] points, initPoints, finalPoints;
                    points = new double[6];
                    initPoints = initTriangles[i][j][k].getAllCoords();
                    finalPoints = finalTriangles[i][j][k].getAllCoords();

                    // Calculate current triangle position based on initial position, final position, and index of frame
                    for (int s = 0; s < 3; s++) {
                        points[2*s] = (finalPoints[2*s] - initPoints[2*s]) * index / (framesPerSecond * seconds) + initPoints[2*s];
                        points[2*s+1] = (finalPoints[2*s+1] - initPoints[2*s+1]) * index / (framesPerSecond * seconds) + initPoints[2*s+1];
                    }

                    // Define triangle from calculated points
                    Triangle dest = new Triangle(points);

                    // Warp triangle
                    MorphTools.warpTriangle(init, tween, initTriangles[i][j][k], dest, null, null);
                }
            }
        }
    }

    // Write passed BufferedImage to dest.jpg
    private void writeJPEG(BufferedImage src, String dest) {
        File f = new File(dest + ".jpg");
        try {
            ImageIO.write(src, "jpg", f);
        } catch (IOException e1) {}
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Get which MyImageObj is being clicked
        MyImageObj myImg = (MyImageObj) e.getSource();

        // If click was on control point
        if (myImg.clickOnPoint(e.getPoint()) != null) {
            // Raise dragging flag
            isDragging = true;
            // Set selectedPoint as point clicked on
            selectedPoint = myImg.clickOnPoint(e.getPoint());
            // Set selectedPoint for both MyImageObjs to change color of points
            imageInitial.setSelectedPoint(selectedPoint[0], selectedPoint[1]);
            imageFinal.setSelectedPoint(selectedPoint[0], selectedPoint[1]);
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        isDragging = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Get which MyImageObj is being dragged on
        MyImageObj myImg = (MyImageObj) e.getSource();

        // If isDragging and current mouse location in the bounds for this control point
        if (isDragging && myImg.pointInBounds(selectedPoint, e.getPoint())) {
            // Set control point to current mouse location
            myImg.setControlPoint(selectedPoint[1], selectedPoint[0], e.getX(), e.getY());
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

}
