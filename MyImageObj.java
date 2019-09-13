import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.Buffer;

public class MyImageObj extends JLabel {

    private BufferedImage img;              // Image
    private BufferedImage filteredImg;
    private int width, height, gridSize;
    private int[][][] controlPoints;        // Positions of control points
    private Polygon[][] controlPointsNodes; // Polygons bounding each draggable control point
    private int[] selectedPoint = {-1, -1}; // Current control point selected
    Color controlPointColor = Color.RED;    // Color of control points/lines
    Color selectedPointColor = Color.YELLOW;// Color of selected point

    public MyImageObj() { }

    // copy constructor
    public MyImageObj(MyImageObj I) {
        // Copy in vars
        width = I.width;
        height = I.height;
        gridSize = I.gridSize;

        // Copy in control points
        controlPoints = new int[gridSize + 2][gridSize + 2][2];
        for (int i = 0; i < gridSize + 2; i++) {
            for (int j = 0; j < gridSize + 2; j++) {
                for (int k = 0; k < 2; k++)
                    controlPoints[i][j][k] = I.controlPoints[i][j][k];
            }
        }

        // Copy in control points polygons
        controlPointsNodes = new Polygon[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                controlPointsNodes[i][j] = new Polygon(I.controlPointsNodes[i][j].xpoints,
                        I.controlPointsNodes[i][j].ypoints, I.controlPointsNodes[i][j].npoints);
            }
        }

        setPreferredSize(new Dimension(width, height));
    }

    public MyImageObj(int w, int h, int g) {
        // Set vars
        width = w;
        height = h;
        gridSize = g;

        // Create grid of control points
        createGrid();

        // Instantiate array of control points nodes
        controlPointsNodes = new Polygon[gridSize][gridSize];

        // Update display to img size
        setPreferredSize(new Dimension(width, height));
        repaint();
    }

    public MyImageObj(BufferedImage i, int w, int h, int g) {
        // Set vars
        img = i;
        filteredImg = i;
        width = w;
        height = h;
        gridSize = g;

        // Create grid of control points
        createGrid();

        // Instantiate array of control points nodes
        controlPointsNodes = new Polygon[gridSize][gridSize];

        // Update display to img size
        setPreferredSize(new Dimension(width, height));
        repaint();
    }

    private void createGrid() {
        // Instantiate array of control points (add 2 to account for border points)
        controlPoints = new int[gridSize + 2][gridSize + 2][2];

        // Evenly space the control points
        for (int i = 0; i < gridSize + 2; i++) {
            for (int j = 0; j < gridSize + 2; j++) {
                controlPoints[i][j][0] = (int)(j * (width / ((double)gridSize + 1)));
                controlPoints[i][j][1] = (int)(i * (height / ((double)gridSize + 1)));
            }
        }
    }

    private void drawGrid(Graphics g) {
        // Loop over non-border points
        for (int i = 1; i < gridSize + 1; i++) {
            for (int j = 1; j < gridSize + 1; j++) {
                drawControlPoint(i, j, g);
            }
        }
    }

    private void drawControlPoint(int i, int j, Graphics g) {
        Graphics2D picture = (Graphics2D) g;
        picture.setColor(controlPointColor);
        // If point is selected point, change color
        if ((selectedPoint[0] == j) && (selectedPoint[1] == i))
            picture.setColor(selectedPointColor);
        picture.setStroke(new BasicStroke(2));

        // Get position of control point
        int x = controlPoints[i][j][0];
        int y = controlPoints[i][j][1];

        // Create points for node to bound control point
        int xPoints[] = {x - 4, x + 4, x + 4, x - 4};
        int yPoints[] = {y - 4, y - 4, y + 4, y + 4};

        // Draw node
        controlPointsNodes[i - 1][j - 1] = new Polygon(xPoints, yPoints, 4);
        g.fillPolygon(controlPointsNodes[i-1][j-1]);
    }

    private void drawLines(Graphics g) {
        Graphics2D picture = (Graphics2D) g;
        picture.setColor(controlPointColor);
        picture.setStroke(new BasicStroke(1));

        // Loop over control points (ignore east and south borders because lines would be out of range)
        for (int i = 0; i < gridSize + 1; i++) {
            for (int j = 0; j < gridSize + 1; j++) {
                // Draw vertical line south if not on west border
                if (i > 0)
                    g.drawLine(controlPoints[j][i][0], controlPoints[j][i][1],
                            controlPoints[j + 1][i][0], controlPoints[j + 1][i][1]);
                // Draw horizontal line east if not on north border
                if (j > 0)
                    g.drawLine(controlPoints[j][i][0], controlPoints[j][i][1],
                            controlPoints[j][i + 1][0], controlPoints[j][i + 1][1]);
                // Draw diagonal line southeast
                g.drawLine(controlPoints[j][i][0], controlPoints[j][i][1],
                        controlPoints[j + 1][i + 1][0], controlPoints[j + 1][i + 1][1]);
            }
        }
    }

    public int[] clickOnPoint(Point click) {
        // Return null if click not on control point
        selectedPoint = null;

        // Loop over clickable control points
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                // If bounding node contains click
                if (controlPointsNodes[j][i].contains(click)) {
                    // Set selected point
                    selectedPoint = new int[2];
                    selectedPoint[0] = j + 1;
                    selectedPoint[1] = i + 1;
                    break;
                }
            }
        }

        return selectedPoint;
    }

    public boolean pointInBounds(int[] selectedPoint, Point p) {
        // Get indices of selected point
        int i = selectedPoint[1];
        int j = selectedPoint[0];
        int[] x = new int[6];
        int[] y = new int[6];

        // Set positions of points that define the bounds of selected point
        x[0] = controlPoints[j-1][i-1][0];
        y[0] = controlPoints[j-1][i-1][1];
        x[1] = controlPoints[j][i-1][0];
        y[1] = controlPoints[j][i-1][1];
        x[2] = controlPoints[j+1][i][0];
        y[2] = controlPoints[j+1][i][1];
        x[3] = controlPoints[j+1][i+1][0];
        y[3] = controlPoints[j+1][i+1][1];
        x[4] = controlPoints[j][i+1][0];
        y[4] = controlPoints[j][i+1][1];
        x[5] = controlPoints[j-1][i][0];
        y[5] = controlPoints[j-1][i][1];

        Polygon bounds = new Polygon(x, y, 6);

        // Return whether the point is in bounds for that control point
        return bounds.contains(p);
    }

    public int[][][] getControlPoints() {
        return controlPoints;
    }

    public void setControlPoints(int[][][] C) {
        controlPoints = C;
    }

    public void setImage(BufferedImage i) {
        if (i == null) return;
        img = i;
        filteredImg = i;
        this.repaint();
    }

    public void setFilteredImg(BufferedImage i) {
        if (i == null) return;
        filteredImg = i;
        this.repaint();
    }

    public BufferedImage getOriginalImg() {
        return img;
    }

    public BufferedImage getImage() {
        return filteredImg;
    }

    public void setControlPoint(int i, int j, int x, int y) {
        controlPoints[j][i][0] = x;
        controlPoints[j][i][1] = y;
        repaint();
    }

    public void setSelectedPoint(int i, int j) {
        selectedPoint = new int[2];
        selectedPoint[0] = j;
        selectedPoint[1] = i;
        repaint();
    }

    public void setWidth(int w) { width = w; }

    public void setHeight(int h) { height = h; }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int g) {
        gridSize = g;
        createGrid();
        controlPointsNodes = new Polygon[gridSize][gridSize];
        repaint();
    }

    public void setControlPointColor(Color c) { controlPointColor = c; }

    public void paintComponent(Graphics g) {
        g.drawImage(filteredImg, 0, 0, width, height, this);
        drawLines(g);
        drawGrid(g);
    }
}