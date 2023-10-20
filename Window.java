import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import javax.swing.*;

public class Window {
  private static void openFile(DrawingPanel panel) {
    JFileChooser fileChooser = new JFileChooser();
    int result = fileChooser.showOpenDialog(null);
    Integer dimX = null;
    Integer dimY = 0;
    Integer i = 0;
    Integer j = 0;
    String line;
    String[] map;
    LinkedList<String[]> lines = new LinkedList<String[]>();
    File file;
    if (result == JFileChooser.APPROVE_OPTION) {
      panel.clearBoard();
      file = fileChooser.getSelectedFile();
      try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        while ((line = br.readLine()) != null) {
          dimY++;
          map = line.split(" ");
          if (dimX == null) {
            dimX = map.length;
          } else if (dimX != map.length) {
            break;
          }
          lines.add(map);
        }
        FieldStates[][] grid = new FieldStates[dimX][dimY];
        for (String[] ln : lines) {
          for (String word : ln) {
            switch (word) {
            case "WALL":
              grid[i][j] = FieldStates.Wall;
              break;
            case "START":
              grid[i][j] = FieldStates.Start;
              panel.setStart();
              break;
            case "FINISH":
              grid[i][j] = FieldStates.Finish;
              panel.setFinish();
              break;
            default:
              grid[i][j] = FieldStates.Empty;
            }
            i++;
          }
          i = 0;
          j++;
        }
        panel.setGrid(grid);
        panel.setXY(dimX, dimY);
      } catch (IOException e) {
        System.err.println("Can't read file " + e.getMessage());
      } catch (Exception e) {
        System.err.println("File is bad.");
      }
    }
  }
  private static void saveFile(DrawingPanel panel) {
    JFileChooser fileChooser = new JFileChooser();
    int result = fileChooser.showSaveDialog(null);
    FieldStates[][] grid;
    if (result == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      grid = panel.getGrid();
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
        int dimX = grid.length;
        int dimY = grid[0].length;

        for (int j = 0; j < dimY; j++) {
          StringBuilder lineBuilder = new StringBuilder();
          for (int i = 0; i < dimX; i++) {
            switch (grid[i][j]) {
            case Wall:
              lineBuilder.append("WALL ");
              break;
            case Start:
              lineBuilder.append("START ");
              break;
            case Finish:
              lineBuilder.append("FINISH ");
              break;
            default:
              lineBuilder.append("EMPTY ");
              break;
            }
          }
          writer.write(lineBuilder.toString().trim());
          writer.newLine();
        }
        System.out.println("File saved successfully.");
      } catch (IOException e) {
        System.err.println("Error saving file: " + e.getMessage());
      }
    }
  }
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
    JMenuItem m33 = new JMenuItem("About");
    DrawingPanel panel = new DrawingPanel();
    m11.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        openFile(panel);
      }
    });
    m22.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        saveFile(panel);
      }
    });
    m33.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(
            m33,
            "Created by Bohdan Borets\nProvided as is\nNo warranty\nContact Information: boretboh@cvut.cz");
      }
    });

    m1.add(m11);
    m1.add(m22);
    m2.add(m33);

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
    Timer timer = new Timer(100, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        panel.repaint();
      }
    });

    timer.start();
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
    squareSize = 20; // Изначальный размер квадрата
    x = 0;
    y = 0;
    grid = new FieldStates[x][y];
    addMouseListener(new SquareClickListener());
  }
  /* private void
   debug ()
   {
     for (int i = 0; i < y; i++)
       {
         for (int j = 0; j < x; j++)
           {
             System.out.print (grid[j][i] + " ");
           }
         System.out.println ();
       }
     System.out.println ();
   }
   */
  private void drawoutline(Integer i, Integer j, Graphics g) {
    g.setColor(Color.GRAY);
    g.drawRect(i * squareSize, j * squareSize, squareSize, squareSize);
  }
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    // debug();
    if (calculator != null)
      grid = calculator.getField();
    for (int i = 0; i < x; i++) {
      for (int j = 0; j < y; j++) {
        if (grid[i][j] == FieldStates.Wall) {
          g.setColor(Color.BLACK);
          g.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);
        } else if (grid[i][j] == FieldStates.Finish) {
          g.setColor(Color.decode("#ba5c28"));
          g.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);
          drawoutline(i, j, g);
        } else if (grid[i][j] == FieldStates.Start) {
          g.setColor(Color.decode("#2886ba"));
          g.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);
          drawoutline(i, j, g);
        } else if (grid[i][j] == FieldStates.Known) {
          g.setColor(Color.decode("#fff900"));
          g.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);
          drawoutline(i, j, g);
        } else if (grid[i][j] == FieldStates.Discovered) {
          g.setColor(Color.decode("#0006ff"));
          g.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);
          drawoutline(i, j, g);
        } else if (grid[i][j] == FieldStates.Solution) {
          g.setColor(Color.decode("#09ff00"));
          g.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);
          if (started && calculator != null) {
            started = false;
            calculator.forceStop();
            calculator = null;
          }
          enable();
          drawoutline(i, j, g);
        } else {
          drawoutline(i, j, g);
        }
      }
    }
  }

  private class SquareClickListener extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
      if (SwingUtilities.isRightMouseButton(e) && enabled) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        int i = mouseX / squareSize;
        int j = mouseY / squareSize;

        if (i >= 0 && i < x && j >= 0 && j < y) {
          if ((grid[i][j] == FieldStates.Empty || grid[i][j] == null) &&
              !startSet) {
            startSet = true;
            grid[i][j] = FieldStates.Start;
          } else if ((grid[i][j] == FieldStates.Empty || grid[i][j] == null) &&
                     !finishSet) {
            finishSet = true;
            grid[i][j] = FieldStates.Finish;
          } else if (grid[i][j] == FieldStates.Start && !finishSet) {
            startSet = false;
            finishSet = true;
            grid[i][j] = FieldStates.Finish;
          } else if (grid[i][j] == FieldStates.Start && finishSet) {
            startSet = false;
            grid[i][j] = FieldStates.Empty;
          } else if (grid[i][j] == FieldStates.Finish) {
            finishSet = false;
            grid[i][j] = FieldStates.Empty;
          }

          repaint();
        }
      } else if (SwingUtilities.isLeftMouseButton(e) && enabled) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        int i = mouseX / squareSize;
        int j = mouseY / squareSize;

        if (i >= 0 && i < x && j >= 0 && j < y) {
          if (grid[i][j] != FieldStates.Start &&
              grid[i][j] != FieldStates.Finish &&
              grid[i][j] != FieldStates.Wall) {
            grid[i][j] = FieldStates.Wall;
          } else if (grid[i][j] == FieldStates.Wall) {
            grid[i][j] = FieldStates.Empty;
          }
          // debug();
          repaint();
        }
      }
    }
  }
  public void enable() { this.enabled = true; }
  public void disable() { this.enabled = false; }
  public void cleanGrid() {
    for (int i = 0; i < y; i++) {
      for (int j = 0; j < x; j++) {
        if (grid[j][i] != FieldStates.Finish &&
            grid[j][i] != FieldStates.Start && grid[j][i] != FieldStates.Wall) {
          grid[j][i] = FieldStates.Empty;
        }
      }
    }
  }
  public void startCalculation() {
    cleanGrid();
    try {
      calculator = new AThread(this.grid);
    } catch (Exception e) {
      calculator = null;
      System.out.println(e.getMessage());
    }
    if (calculator != null) {
      disable();
      calculator.setDaemon(true);
      calculator.start();
      started = true;
    }
  }
  public void calculate() {
    if (started) {
      stopCalculation();
    }
    startCalculation();
    if (calculator != null) {
      calculator.solveAsync();
    }
    repaint();
  }
  public void processMove() {
    if (!started) {
      startCalculation();
    }
    if (calculator != null && calculator.allowMove()) {
      calculator.forceStop();
      grid = calculator.getField();
      calculator = null;
      started = false;
      enable();
    }
    repaint();
  }
  private void stopCalculation() {
    calculator.forceStop();
    calculator.interrupt();
    calculator = null;
    started = false;
  }
  public void clearBoard() {
    if (calculator != null) {
      calculator.forceStop();
      calculator.interrupt();
      calculator = null;
      started = false;
    }
    enable();
    startSet = false;
    finishSet = false;
    x = getWidth() / squareSize;
    y = getHeight() / squareSize;
    grid = new FieldStates[x][y];
    cleanGrid();
    repaint();
  }
  public void setGrid(FieldStates[][] grid) { this.grid = grid; }
  public void setXY(int x, int y) {
    this.x = x;
    this.y = y;
  }
  public void setStart() { startSet = true; }
  public void setFinish() { finishSet = true; }
  public FieldStates[][] getGrid() { return grid; }
}
