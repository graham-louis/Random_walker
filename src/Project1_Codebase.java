import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class Project1_Codebase {
    //references to some variables we may want to access in a global context
    static int WIDTH = 500; //width of the image
    static int HEIGHT = 500; //height of the image
    static BufferedImage Display; //the image we are displaying
    static JFrame window; //the frame containing our window


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
                JLabel Render = new JLabel("Path Render:");
                Configuration.add(Render);
                ButtonGroup Renderers = new ButtonGroup();
                JRadioButton Black = new JRadioButton("Black");
                Black.setActionCommand("black");

                Black.setSelected(true);
                JRadioButton Gradient = new JRadioButton("Gradient");
                Gradient.setActionCommand("gradient");

                JLabel c1 = new JLabel("Gradient Start:");
                JTextField color1 = new JTextField("0000FF");
                JLabel c2 = new JLabel("Gradient End:");
                JTextField color2 = new JTextField("FFAA00");
                Renderers.add(Black);
                Renderers.add(Gradient);

                Configuration.add(Black);
                Configuration.add(Gradient);
                Configuration.add(c1);
                Configuration.add(color1);
                Configuration.add(c2);
                Configuration.add(color2);

                //Run Button
                JButton Run = new JButton("Walk");
                Run.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int count = Integer.parseInt(StepCount.getText()); //get from a TextField, and read as an int
                        String walk_type = WalkerTypes.getSelection().getActionCommand();//gets the action command of which radio button is selected
                        String geom_type = Geometries.getSelection().getActionCommand();
                        String render_type = Renderers.getSelection().getActionCommand();
                        int grad_start = (int)Long.parseLong(color1.getText(), 16);//Hex string to int
                        int grad_end = (int)Long.parseLong(color2.getText(), 16);//Hex string to int

                        //===Walk, Update Display, repaint===
                        //1. Generate a Buffered image using the data from above (return one from a method)?

                        if (walk_type == "standard") { //if standard walk
                            UpdateDisplay(Walk(count, geom_type, render_type, grad_start, grad_end));
                        }
                        else { //if picky walk
                            UpdateDisplay(PickyWalk(count, geom_type, render_type, grad_start, grad_end));
                        }
                        window.repaint();
                    }
                });

                Configuration.add(Run);
                window.add(Configuration,BorderLayout.EAST);

            }
        });
    }

    

    static BufferedImage Walk(int count, String geom_type, String render_type, int grad_start, int grad_end) {
        //Create a new image to return
        int gradient[] = getGradient(grad_start, grad_end, count); //get gradient array
        BufferedImage img = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_ARGB); //create image

        //position of the walker
        int x = WIDTH / 2; //start in middle
        int y = HEIGHT / 2;

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
                    x = WIDTH - 1;
                }
                if (x > WIDTH - 1) {
                    x = 0;
                }
                if (y < 0) {
                    y = HEIGHT - 1;
                }
                if (y > HEIGHT - 1) {
                    y = 0;
                }
            }
            if (geom_type.equals("bounded")) {
                if (x < 0 && y < 0){ x++; y++; continue;}// bottom left corner
                if (x > WIDTH - 1 && y < 0){ x--; y++; continue;} //bottom right corner
                if (x < 0 && y > HEIGHT - 1){ x++; y--; continue;} //top left corner
                if (x > WIDTH - 1 && y > HEIGHT - 1){ x--; y--; continue;} //top right corner

                else if (x < 0) x++; //left edge
                else if (y < 0) y++; //bottom edge
                else if (x > WIDTH - 1) x--; //right edge
                else if (y > HEIGHT - 1) y--; //top edge
            }

            // If the renderer is black, color the pixel you are on black
            if (render_type.equals("black")) {
                    img.setRGB(x,y, Color.BLACK.getRGB());
            }

            // If the renderer is gradient, color the pixel you are on with a gradient from grad_start to grad_end
            if (render_type.equals("gradient")) {
                    img.setRGB(x,y,gradient[i]);
            }
        }
        return img;
    }

    static BufferedImage PickyWalk(int count, String geom_type, String render_type, int grad_start, int grad_end) {
        //Create a new image to return
        int gradient[] = getGradient(grad_start, grad_end, count);
        BufferedImage img = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_ARGB);

        //position of the walker
        int x = WIDTH / 2;
        int y = HEIGHT / 2;

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
                        x = WIDTH - 1;
                    }
                    if (x > WIDTH - 1) {
                        x = 0;
                    }
                    if (y < 0) {
                        y = HEIGHT - 1;
                    }
                    if (y > HEIGHT - 1) {
                        y = 0;
                    }
                }
                if (geom_type.equals("bounded")) {
                    if (x < 0 && y < 0){ x++; y++; continue;}// bottom left corner
                    if (x > WIDTH - 1 && y < 0){ x--; y++; continue;} //bottom right corner
                    if (x < 0 && y > HEIGHT - 1){ x++; y--; continue;} //top left corner
                    if (x > WIDTH - 1 && y > HEIGHT - 1){ x--; y--; continue;} //top right corner

                    else if (x < 0) x++; //left edge
                    else if (y < 0) y++; //bottom edge
                    else if (x > WIDTH - 1) x--; //right edge
                    else if (y > HEIGHT - 1) y--; //top edge
                }

                // If the renderer is black, color the pixel you are on black
                if (render_type.equals("black")) {
                    img.setRGB(x,y, Color.BLACK.getRGB());
                }

                // If the renderer is gradient, color the pixel you are on with a gradient from grad_start to grad_end
                if (render_type.equals("gradient")) {
                    try {
                        img.setRGB(x,y,gradient[i]);
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("Array out of bounds exception");
                        //toroidal, gradient, picky cause issue
                    }

                }
            }
        }
        return img;

    }

    //gradient method and helper
    static int extractChannel(int color, int shift){
        return (color >> shift) & 0xFF; //shifts color right by shift, then ANDs with 0xFF to get the last 8 bits
    }

    //returns an array of ints representing a gradient from start to end with length steps
    static int[] getGradient(int start, int end, int steps){
        int gradient[] = new int[steps]; //create array of ints to hold gradient with length of steps

        //extracts the red, green, and blue channels from the start and end colors
        int r1 = extractChannel(start, 16);
        int g1 = extractChannel(start, 8);
        int b1 = extractChannel(start, 0);
        int r2 = extractChannel(end, 16);
        int g2 = extractChannel(end, 8);
        int b2 = extractChannel(end, 0);

        //calculates the difference between the start and end colors
        float rstep_dif = (float) (r2 - r1) / steps;
        float gstep_dif = (float) (g2 - g1) / steps;
        float bstep_dif = (float) (b2 - b1) / steps;

        //sets the initial values of the red, green, and blue channels
        float r = r1;
        float g = g1;
        float b = b1;

        for (int i = 0; i < steps; i++) { //for each step
            gradient[i] = new Color((int)r, (int)g, (int)b).getRGB(); //set the color of the gradient at the current step

            //increment the red, green, and blue channels by the difference between the start and end colors
            r += rstep_dif;
            g += gstep_dif;
            b += bstep_dif;
        }

        return gradient;
    }

    //A method to update the display image to match one generated by you
    static void UpdateDisplay(BufferedImage img){
        //Below 4 lines draws the input image on the display image
        Graphics2D g = (Graphics2D) Display.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0,0,WIDTH,HEIGHT);
        g.drawImage(img,0,0,null);

        //forces the window to redraw its components (i.e., the image)
        window.repaint();
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

}
































