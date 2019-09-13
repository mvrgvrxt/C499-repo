import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class PreviewWindow extends JFrame{
    private MyImageObj grid;        // MyImageObj to show control point grid animation
    private int[][][] controlPointsInitial, controlPointsFinal;
    private Timer animationTimer;   // timer to drive animation
    private int framesPerSecond, seconds, frames, count = 0;
    private int frameLength;        // length of a frame in milliseconds

    public PreviewWindow(MyImageObj i, MyImageObj f, int fps, int s, boolean isMorph) {
        super("Preview");

        // Instantiate grid using copy constructor on initial MyImageObj i
        grid = new MyImageObj(i);
        // Get controlPoints arrays for initial and final
        controlPointsInitial = i.getControlPoints();
        controlPointsFinal = f.getControlPoints();

        // Set variables
        framesPerSecond = fps;
        seconds = s;
        updateFrames();

        // Instantiate animationTimer
        animationTimer = new Timer(frameLength, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // If animation is not yet complete
                if (count < frames) {

                    // Load rendered morph frames if window isMorph
                    if (isMorph) {
                        grid.setImage(readImage("renders/" + Integer.toString(count) + ".jpg"));
                    }

                    // Move control points a frame
                    moveFrame();

                    // Increment frame count
                    count++;
                }
                else {
                    // Stop animation
                    animationTimer.stop();
                }
            }
        });

        Container c = getContentPane();

        // Add grid to frame
        c.add(grid);

        pack();
        setVisible(true);
    }

    private void moveFrame() {
        // If frame count reaches number of requested frames
        if (count == frames) {
            // Set to points to final position
            grid.setControlPoints(controlPointsFinal);
        }
        else {
            double x, y;
            // Loop over control points nodes
            for (int i = 1; i < grid.getGridSize() + 1; i++) {
                for (int j = 1; j < grid.getGridSize() + 1; j++) {
                    // Calculate position that is count/frames of the way between initial position and final position
                    x = ((double)(controlPointsFinal[i][j][0] - controlPointsInitial[i][j][0]) * count / frames) +
                            controlPointsInitial[i][j][0];
                    y = ((double)(controlPointsFinal[i][j][1] - controlPointsInitial[i][j][1]) * count / frames) +
                            controlPointsInitial[i][j][1];
                    // Update control point to new position
                    grid.setControlPoint(j, i, (int)x, (int)y);
                }
            }
        }
        repaint();
    }

    private void updateFrames() {
        // Calculate frame and frameLength based on current value of seconds and framesPerSecond
        frames = seconds * framesPerSecond;
        frameLength = 1000 / framesPerSecond;
    }

    public void reset() {
        grid.setControlPoints(controlPointsInitial);
        count = 0;
    }

    public BufferedImage readImage (String file) {

        // Read image file
        Image image = Toolkit.getDefaultToolkit().getImage(file);

        // Throw exception on error reading file
        MediaTracker tracker = new MediaTracker (new Component () {});
        tracker.addImage(image, 0);
        try { tracker.waitForID (0); }
        catch (InterruptedException e) {}

        // Create new BufferImage with dimensions of image
        BufferedImage img = new BufferedImage(image.getWidth(this), image.getHeight(this),
                BufferedImage.TYPE_INT_RGB);

        // Draw image on BufferImage
        Graphics2D big = img.createGraphics();
        big.drawImage (image, 0, 0, this);

        return img;
    }

    public void startTimer() {
        animationTimer.start();
    }

    public void setFramesPerSecond(int fps) {
        framesPerSecond = fps;
        updateFrames();
        animationTimer.setDelay(frameLength);
    }

    public void setSeconds(int s) {
        seconds = s;
        updateFrames();
        animationTimer.setDelay(frameLength);
    }
}
