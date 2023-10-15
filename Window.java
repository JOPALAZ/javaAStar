import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Window {
    public static void main(String args[]) {
        JFrame frame = new JFrame("Drawing Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        JMenuBar mb = new JMenuBar();
        JMenu m1 = new JMenu("FILE");
        JMenu m2 = new JMenu("Help");
        mb.add(m1);
        mb.add(m2);
        JMenuItem m11 = new JMenuItem("Open");
        JMenuItem m22 = new JMenuItem("Save as");
        m1.add(m11);
        m1.add(m22);

        DrawingPanel panel = new DrawingPanel();
        panel.setLayout(new BorderLayout());
        JPanel upperPanel = new JPanel();
        JPanel lowerPanel = new JPanel();
        JButton reset = new JButton("Reset");
        JButton calculate = new JButton("Calculate");
        JButton process = new JButton("Process");
        reset.addActionListener(e -> panel.clearBoard());
        calculate.addActionListener(e -> panel.calculate());
        process.addActionListener(e -> panel.processMove());
        upperPanel.add(reset);
        upperPanel.add(mb);
        lowerPanel.add(calculate);
        lowerPanel.add(process);
        
        

        frame.getContentPane().add(BorderLayout.NORTH, upperPanel);
        frame.getContentPane().add(BorderLayout.SOUTH, lowerPanel);
        frame.getContentPane().add(BorderLayout.CENTER, panel);

        frame.setVisible(true);
        Timer timer = new Timer(100, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.repaint(); // Перерисовываем панель каждую секунду
            }
        });
        
        timer.start(); // Запускаем таймер
    }
}

class DrawingPanel extends JPanel {
    private int squareSize;
    private FieldStates[][] grid;
    private int x;
    private int y;
    private boolean enabled = true;
    private boolean startSet = false;
    private boolean finishSet = false;
    private boolean started = false;
    private AThread calculator;
    public DrawingPanel() {
        setPreferredSize(new Dimension(400, 400));
        squareSize = 20;  // Изначальный размер квадрата
        x = 0;
        y = 0;
        grid = new FieldStates[x][y];
        addMouseListener(new SquareClickListener());
    }
    private void debug()
    {
        
        for(int i=0; i<y; i++) 
        {
            for(int j=0; j<x; j++)
            {
                System.out.print(grid[j][i] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
    private void drawoutline(Integer i, Integer j, Graphics g)
    {
            g.setColor(Color.GRAY);
            g.drawRect(i * squareSize, j * squareSize, squareSize, squareSize);
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //debug();
        if(calculator != null)
            grid=calculator.getField();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if (grid[i][j] == FieldStates.Wall) 
                {
                    g.setColor(Color.BLACK);
                    g.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);
                }
                else if (grid[i][j] == FieldStates.Finish) 
                {
                    g.setColor(Color.decode("#ba5c28"));
                    g.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);
                    drawoutline(i, j, g);
                }
                else if (grid[i][j] == FieldStates.Start)
                {
                    g.setColor(Color.decode("#2886ba"));
                    g.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);
                    drawoutline(i, j, g);
                }
                else if (grid[i][j] == FieldStates.Known)
                {
                    g.setColor(Color.decode("#fff900"));
                    g.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);
                    drawoutline(i, j, g);
                }
                else if (grid[i][j] == FieldStates.Discovered)
                {
                    g.setColor(Color.decode("#0006ff"));
                    g.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);
                    drawoutline(i, j, g);
                }
                else if (grid[i][j] == FieldStates.Solution)
                {
                    g.setColor(Color.decode("#09ff00"));
                    g.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);
                    drawoutline(i, j, g);
                }
                else 
                {
                    drawoutline(i, j, g);
                }
            }
        }
    }

    private class SquareClickListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)&&enabled) 
            {
                int mouseX = e.getX();
                int mouseY = e.getY();

                int i = mouseX / squareSize;
                int j = mouseY / squareSize;

                if (i >= 0 && i < x && j >= 0 && j < y) 
                {
                    if((grid[i][j] == FieldStates.Empty || grid[i][j] == null)&&!startSet)
                    {
                        startSet = true;
                        grid[i][j] = FieldStates.Start;
                    }
                    else if((grid[i][j] == FieldStates.Empty || grid[i][j] == null)&&!finishSet)
                    {
                        finishSet = true;
                        grid[i][j] = FieldStates.Finish;
                    }
                    else if(grid[i][j] == FieldStates.Start && !finishSet)
                    {
                        startSet = false;
                        finishSet = true;
                        grid[i][j] = FieldStates.Finish;
                    }
                    else if(grid[i][j] == FieldStates.Start && finishSet)
                    {
                        startSet = false;
                        grid[i][j] = FieldStates.Empty;
                    }
                    else if(grid[i][j] == FieldStates.Finish)
                    {
                        finishSet = false;
                        grid[i][j] = FieldStates.Empty;
                    }
                      
                    repaint();
                }
            }
            else if(SwingUtilities.isLeftMouseButton(e)&&enabled)
            {
                int mouseX = e.getX();
                int mouseY = e.getY();

                int i = mouseX / squareSize;
                int j = mouseY / squareSize;

                if (i >= 0 && i < x && j >= 0 && j < y) 
                {
                    if(grid[i][j] != FieldStates.Start && grid[i][j] != FieldStates.Finish && grid[i][j] != FieldStates.Wall) 
                    {
                        grid[i][j] = FieldStates.Wall;  
                    }
                    else if(grid[i][j] == FieldStates.Wall)
                    {
                        grid[i][j] = FieldStates.Empty;
                    }
                    //debug();
                    repaint();
                }
            }
        }
    }
    public void enable()
    {
        this.enabled = true;
    }
    public void disable()
    {
        this.enabled = false;
    }
    public void cleanGrid()
    {
        for (int i = 0; i < y; i++)
        {
            for (int j = 0; j < x; j++)
            {
                if(grid[j][i] != FieldStates.Finish && grid[j][i] != FieldStates.Start && grid[j][i] != FieldStates.Wall)
                {
                    grid[j][i] = FieldStates.Empty;
                }
            }
        }
    }
    public void startCalculation()
    {
        cleanGrid();
        try
        {
            calculator = new AThread(this.grid);
        }
        catch (Exception e)
        {
            calculator = null;
            System.out.println(e.getMessage());
        }
        if (calculator!=null)
        {
            disable();
            calculator.setDaemon(true);
            calculator.start();
            started=true;
        }
    }
    public void calculate()
    {
        if(!started)
        {
            startCalculation();
        }
        if(calculator != null)
        {
            calculator.solve();
            calculator.forceStop();
            grid = calculator.getField();
            calculator = null;
            started=false;
        }
        enable();
        repaint();
    }
    public void processMove()
    {
        if(!started)
        {
            startCalculation();
        }
        if(calculator!=null && calculator.allowMove())
        {
            calculator.forceStop();
            grid = calculator.getField();
            calculator = null;
            started=false;
            enable();
        }
        repaint();
    }
    public void clearBoard() {
        if(calculator != null)
        {
            calculator.forceStop();
            calculator.interrupt();
            calculator = null;
        }
        enable();
        startSet=false;
        finishSet=false;
        x = getWidth() / squareSize;
        y = getHeight() / squareSize;

        grid = new FieldStates[x][y];
        repaint();
    }
}