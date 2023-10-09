import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.image.BufferedImageOp;

public class Project2_Codebase {
    //references to some variables we may want to access in a global context
    static int WIDTH = 500; //width of the image
    static int HEIGHT = 500; //height of the image
    static BufferedImage Display; //the image we are displaying
    static JFrame window; //the frame containing our window

    static Graphics2D g2d;

    static float[][] terrain;

    public static void main(String[] args) {
        //run the GUI on the special event dispatch thread (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Create the window and set options
                //The window
                window = new JFrame("RandomWalker");
                window.setPreferredSize(new Dimension(WIDTH+100,HEIGHT+50));
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.setVisible(true);
                window.pack();


                //Display panel/image
                JPanel DisplayPanel = new JPanel();
                Display = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_ARGB);
                g2d = Display.createGraphics();
                DisplayPanel.add(new JLabel(new ImageIcon(Display)));
                window.add(DisplayPanel,BorderLayout.CENTER);

                //Config panel
                JPanel Configuration = new JPanel();
                Configuration.setBackground(new Color(230,230,230));
                Configuration.setPreferredSize(new Dimension(100,500));
                Configuration.setLayout(new FlowLayout());

                //Step count input
                JLabel StepCountLabel = new JLabel("Step Count:");
                Configuration.add(StepCountLabel);

                JTextField StepCount = new JTextField("500");
                StepCount.setPreferredSize(new Dimension(100,25));
                Configuration.add(StepCount);

                //Walker type input
                JLabel WalkerType = new JLabel("Walker Type:");
                Configuration.add(WalkerType);

                ButtonGroup WalkerTypes = new ButtonGroup();//group of buttons
                JRadioButton Standard = new JRadioButton("Standard");//creates a radio button. in a ButtonGroup, only one can be selected at a time
                Standard.setActionCommand("standard");//can be grabbed to see which button is active
                Standard.setSelected(true);//set this one as selected by default
                JRadioButton Picky = new JRadioButton("Picky");
                Picky.setActionCommand("picky");
                WalkerTypes.add(Standard); //add to panel
                WalkerTypes.add(Picky);
                Configuration.add(Standard); //set as part of group
                Configuration.add(Picky);

                //Walker type input
                JLabel Geometry = new JLabel("World Geometry:");
                Configuration.add(Geometry);
                ButtonGroup Geometries = new ButtonGroup();
                JRadioButton Bounded = new JRadioButton("Bounded");
                Bounded.setActionCommand("bounded");
                Bounded.setSelected(true);
                JRadioButton Toroidal = new JRadioButton("Toroidal");
                Toroidal.setActionCommand("toroidal");
                Geometries.add(Bounded);
                Geometries.add(Toroidal);
                Configuration.add(Bounded);
                Configuration.add(Toroidal);

                //Path Rendering input
                JLabel Render = new JLabel("Render Style:");
                Configuration.add(Render);
                ButtonGroup Renderers = new ButtonGroup();
                JRadioButton Satellite = new JRadioButton("Satellite");
                Satellite.setActionCommand("satellite");


                Satellite.setSelected(true);
                JRadioButton Height = new JRadioButton("Height");
                Height.setActionCommand("height");

                Renderers.add(Satellite);
                Renderers.add(Height);

                Configuration.add(Satellite);
                Configuration.add(Height);

                //Dimension input
                JLabel DimensionLabel = new JLabel("Dimensions:");
                Configuration.add(DimensionLabel);

                JTextField Dimension = new JTextField("50");
                Dimension.setPreferredSize(new Dimension(100,25));
                Configuration.add(Dimension);

                Satellite.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int dimension = Integer.parseInt(Dimension.getText());
                        renderTerrain("satellite", Integer.parseInt(Dimension.getText()));
                        g2d.drawImage(Display,0,0,null);
                        window.repaint();
                    }
                });

                Height.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int dimension = Integer.parseInt(Dimension.getText());
                        renderTerrain("height", Integer.parseInt(Dimension.getText()));
                        g2d.drawImage(Display,0,0,null);
                        window.repaint();
                    }
                });


                //Run Button
                JButton Run = new JButton("Walk");
                Run.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int count = Integer.parseInt(StepCount.getText()); //get from a TextField, and read as an int
                        String walk_type = WalkerTypes.getSelection().getActionCommand();//gets the action command of which radio button is selected
                        String geom_type = Geometries.getSelection().getActionCommand();
                        String render_type = Renderers.getSelection().getActionCommand();
                        int dimension = Integer.parseInt(Dimension.getText());

                        //===Walk, Update Display, repaint===
                        //1. Generate a Buffered image using the data from above (return one from a method)?

                        terrain = new float[dimension][dimension];

                        if (walk_type == "standard") { //if standard walk
                            Walk(count, geom_type, render_type, dimension, terrain);
                            g2d.drawImage(Display,0,0,null);
                        }
                        else { //if picky walk
                            PickyWalk(count, geom_type, render_type, dimension);
                            g2d.drawImage(Display,0,0,null);
                        }
                        window.repaint();
                    }
                });

                Configuration.add(Run);

                //Divide button
                JButton Divide = new JButton("Divide");

                //dimension = dimension * 2, values at [i,j] move to [i*2,j*2], other values are nearest neighbor interpolated
                //smooth terrain by sweeping 3x3 gaussian blur kernel over it, use edge extension
                Divide.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String render_type = Renderers.getSelection().getActionCommand();
                        Dimension.setText(Integer.toString(Integer.parseInt(Dimension.getText()) * 2));
                        int dimension = Integer.parseInt(Dimension.getText());
                        float[][] newTerrain = interpolateTerrain(terrain);
                        terrain = newTerrain;
                        terrain = gaussianBlur(terrain);


                        renderTerrain(render_type, dimension);
                        g2d.drawImage(Display,0,0,null);
                        window.repaint();

                    }
                });

                Configuration.add(Divide);

                window.add(Configuration,BorderLayout.EAST);

            }
        });
    }

    static void Walk(int count, String geom_type, String render_type, int dimension, float[][] terrain) {

        //position of the walker
        int x = dimension / 2; //start in middle
        int y = dimension / 2;

        for (int i = 0; i < count; i++) { //for each step

            //Generate a random direction (0-9)
            int direction = (int) (Math.random() * 10);
            switch (direction) //move in that direction
            {
                case 0:
                    x--;
                    y--;
                    break;
                case 1:
                    y--;
                    break;
                case 2:
                    x++;
                    y--;
                    break;
                case 3:
                    x--;
                    break;
                case 4:
                    break;
                case 5:
                    x++;
                    break;
                case 6:
                    x--;
                    y++;
                    break;
                case 7:
                    y++;
                    break;
                case 8:
                    x++;
                    y++;
                    break;
            }

            // If the geometry is toroidal, check if you are out of bounds, and if so, move to the opposite side of the image
            if (geom_type.equals("toroidal")) {
                if (x < 0) {
                    x = dimension - 1;
                }
                if (x > dimension - 1) {
                    x = 0;
                }
                if (y < 0) {
                    y = dimension - 1;
                }
                if (y > dimension - 1) {
                    y = 0;
                }
            }
            if (geom_type.equals("bounded")) {
                if (x < 0 && y < 0){ x++; y++; continue;}// bottom left corner
                if (x > dimension - 1 && y < 0){ x--; y++; continue;} //bottom right corner
                if (x < 0 && y > dimension - 1){ x++; y--; continue;} //top left corner
                if (x > dimension - 1 && y > dimension - 1){ x--; y--; continue;} //top right corner

                else if (x < 0) x++; //left edge
                else if (y < 0) y++; //bottom edge
                else if (x > dimension - 1) x--; //right edge
                else if (y > dimension - 1) y--; //top edge
            }

            //modify terrain
            try {
                terrain[x - 1][y - 1] += 1.0f;
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            try {
                terrain[x - 1][y] += 1.0f;
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            try {
                terrain[x - 1][y + 1] += 1.0f;
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            try {
                terrain[x][y - 1] += 1.0f;
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            try {
                terrain[x][y] += 1.0f;
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            try {
                terrain[x][y + 1] += 1.0f;
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            try {
                terrain[x + 1][y - 1] += 1.0f;
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            try {
                terrain[x + 1][y] += 1.0f;
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            try {
                terrain[x + 1][y + 1] += 1.0f;
            } catch (ArrayIndexOutOfBoundsException e) {
            }
        }
        renderTerrain(render_type, dimension);
    }

    //draw dimension x dimension # of Squares on img whose color is determined by the value of terrain
    static void renderTerrain(String render_type, int dimension){
        float max = findMax(terrain, dimension);

        float widthCalc = (float)WIDTH/(float)dimension;

        if (render_type == "satellite") {
            //loop through each square in the terrain
            for (int i = 0; i < dimension; i++) {
                for (int j = 0; j < dimension; j++) {
                    //determine color based on value of
                    int color;
                    if (terrain[i][j] > max * 0.75) color = new Color(230, 230, 255).getRGB();
                    else if (terrain[i][j] > max * 0.5) color = new Color(100, 75, 50).getRGB();
                    else if (terrain[i][j] > max * 0.1) color = new Color(30, 150, 30).getRGB();
                    else if (terrain[i][j] > max * 0.05) color = new Color(255, 255, 195).getRGB();
                    else color = new Color(27, 228, 255).getRGB();

                    //draw square
                    Rectangle2D rect = new Rectangle2D.Float(i * widthCalc, j * widthCalc, widthCalc, widthCalc);
                    g2d.setColor(new Color(color));
                    g2d.fill(rect);
                }
            }
        }
        if (render_type == "height") {
            //get black-white gradient
            int[] gradient = getGradient((int)max);

            //loop through each square in the terrain
            for (int i = 0; i < dimension; i++) {
                for (int j = 0; j < dimension; j++){
                    //draw square
                    Rectangle2D rect = new Rectangle2D.Float(i * widthCalc, j * widthCalc, widthCalc, widthCalc);
                    try {
                        g2d.setColor(new Color(gradient[(int)terrain[i][j]]));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        g2d.setColor(new Color(gradient[(int)max]));
                    }
                    g2d.fill(rect);
                }
            }

        }
    }

    static void PickyWalk(int count, String geom_type,  String render_type, int dimension) {

        //position of the walker
        int x = dimension / 2; //start in middle
        int y = dimension / 2;

        for (int i = 0; i < count; i++) {
            // Generate a random direction (0-9)
            int direction = (int) (Math.random() * 10);
            // Generate a random number of steps (1-10)
            int steps = (int) (Math.random() * 10) + 1;
            for (int j = 0; j < steps; j++) { //perfrom standard step for each step in chosen direction
                i++;
                if(i == count) break; //if reached end of count break to prevent out of bounds error
                switch (direction) //move in that direction
                {
                    case 0:
                        x--;
                        y--;
                        break;
                    case 1:
                        y--;
                        break;
                    case 2:
                        x++;
                        y--;
                        break;
                    case 3:
                        x--;
                        break;
                    case 4:
                        break;
                    case 5:
                        x++;
                        break;
                    case 6:
                        x--;
                        y++;
                        break;
                    case 7:
                        y++;
                        break;
                    case 8:
                        x++;
                        y++;
                        break;
                }

                // If the geometry is toroidal, check if you are out of bounds, and if so, move to the opposite side of the image
                if (geom_type.equals("toroidal")) {
                    if (x < 0) {
                        x = dimension - 1;
                    }
                    if (x > dimension - 1) {
                        x = 0;
                    }
                    if (y < 0) {
                        y = dimension - 1;
                    }
                    if (y > dimension - 1) {
                        y = 0;
                    }
                }
                if (geom_type.equals("bounded")) {
                    if (x < 0 && y < 0){ x++; y++; continue;}// bottom left corner
                    if (x > dimension - 1 && y < 0){ x--; y++; continue;} //bottom right corner
                    if (x < 0 && y > dimension - 1){ x++; y--; continue;} //top left corner
                    if (x > dimension - 1 && y > dimension - 1){ x--; y--; continue;} //top right corner

                    else if (x < 0) x++; //left edge
                    else if (y < 0) y++; //bottom edge
                    else if (x > dimension - 1) x--; //right edge
                    else if (y > dimension - 1) y--; //top edge
                }
                //modify terrain
                try {
                    terrain[x - 1][y - 1] += 1.0f;
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                try {
                    terrain[x - 1][y] += 1.0f;
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                try {
                    terrain[x - 1][y + 1] += 1.0f;
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                try {
                    terrain[x][y - 1] += 1.0f;
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                try {
                    terrain[x][y] += 1.0f;
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                try {
                    terrain[x][y + 1] += 1.0f;
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                try {
                    terrain[x + 1][y - 1] += 1.0f;
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                try {
                    terrain[x + 1][y] += 1.0f;
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                try {
                    terrain[x + 1][y + 1] += 1.0f;
                } catch (ArrayIndexOutOfBoundsException e) {
                }

            }


        }

        renderTerrain(render_type, dimension);
    }

    static int[] getGradient(int range){
        int gradient[] = new int[range + 1]; //create array of ints to hold gradient with size of range

        //calculates the difference between the start and end colors
        float rstep_dif = (float) 255 / range;
        float gstep_dif = (float) 255 / range;
        float bstep_dif = (float) 255 / range;

        //sets the initial values of the red, green, and blue channels
        float r = 0;
        float g = 0;
        float b = 0;

        for (int i = 0; i < range + 1; i++) { //for each step
            gradient[i] = new Color((int)r, (int)g, (int)b).getRGB(); //set the color of the gradient at the current step

            //increment the red, green, and blue channels by the difference between the start and end colors
            r += rstep_dif;
            g += gstep_dif;
            b += bstep_dif;
        }

        return gradient;
    }

    //gradient method and helper
    static int extractChannel(int color, int shift){
        return (color >> shift) & 0xFF; //shifts color right by shift, then ANDs with 0xFF to get the last 8 bits
    }

    //find max
    static float findMax(float[][] terrain, int dimension){
        float max = 0;
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++){
                if (terrain[i][j] > max) max = terrain[i][j];
            }
        }
        return max;
    }

    public static float[][] interpolateTerrain(float[][] oldTerrain) {
        int oldRows = oldTerrain.length;
        int oldCols = oldTerrain[0].length;
        int newRows = oldRows * 2;
        int newCols = oldCols * 2;

        float[][] newTerrain = new float[newRows][newCols];

        for (int i = 0; i < newRows; i++) {
            for (int j = 0; j < newCols; j++) {
                int oldI = i / 2; // Map new row index to old row index
                int oldJ = j / 2; // Map new column index to old column index

                if (oldI < oldRows && oldJ < oldCols) {
                    newTerrain[i][j] = oldTerrain[oldI][oldJ];
                } else {
                    newTerrain[i][j] = 0; // Default value for out-of-bounds indices
                }
            }
        }

        return newTerrain;
    }
    public static float[][] gaussianBlur(float[][] oldTerrain) {
        int rows = oldTerrain.length;
        int cols = oldTerrain[0].length;
        float[][] newTerrain = new float[rows][cols];

        float[][] kernel = {{0.0625f, 0.125f, 0.0625f}, {0.125f, 0.25f, 0.125f}, {0.0625f, 0.125f, 0.0625f}};

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                float sum = 0;
                for (int k = -1; k < 2; k++) {
                    for (int l = -1; l < 2; l++) {
                        //apply edge extension
                        int x = i + k;
                        int y = j + l;
                        if (x < 0) x = 0;
                        if (x > rows - 1) x = rows - 1;
                        if (y < 0) y = 0;
                        if (y > cols - 1) y = cols - 1;
                        sum += oldTerrain[x][y] * kernel[k + 1][l + 1];
                    }
                }
                newTerrain[i][j] = sum;
            }
    }
        return newTerrain;
    }

}

