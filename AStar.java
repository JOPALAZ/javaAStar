import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

class smartQueue {

  List<Node> queue;
  public smartQueue() { this.queue = new LinkedList<>(); }
  public boolean addElemet(Node candidate) {
    Coordinate coordinate = candidate.getPos();
    boolean trigered = false;
    for (Node resident : queue) {
      if (resident.getPos() == coordinate) {
        trigered = true;
        if (resident.getFCost() == candidate.getFCost() &&
                resident.getHCost() > candidate.getHCost() ||
            resident.getFCost() > candidate.getFCost()) {
          queue.remove(resident);
          queue.add(candidate);
          return true;
        }
      }
    }
    if (!trigered) {
      queue.add(candidate);
      return true;
    }
    return false;
  }
  public Node pollLowest() {
    if (queue.size() == 0) {
      return null;
    }
    Node lowest = queue.get(queue.size() - 1);
    for (Node resident : queue) {
      if (lowest.getFCost() > resident.getFCost() ||
          (resident.getFCost() == lowest.getFCost() &&
           lowest.getHCost() > resident.getHCost())) {
        lowest = resident;
      }
    }
    //System.out.printf("F = %d H = %d \n", lowest.getFCost(), lowest.getHCost());
    queue.remove(lowest);
    return lowest;
  }

  public Node peekLowest() {
    if (queue.size() == 0) {
      return null;
    }
    Node lowest = queue.get(queue.size() - 1);
    for (Node resident : queue) {
      if (lowest.getFCost() > resident.getFCost() ||
          (resident.getFCost() == lowest.getFCost() &&
           lowest.getHCost() > resident.getHCost())) {
        lowest = resident;
      }
    }
    queue.remove(lowest);
    return lowest;
  }
  public Integer size() { return queue.size(); }
}
class Node {
  private Coordinate father;
  private Integer GCost;
  private Integer HCost;
  private Integer FCost;
  private FieldStates state;
  private Coordinate pos;
  public Node(Coordinate father, Integer cost, FieldStates state,
              Coordinate pos) {
    this.father = father;
    this.GCost = cost;
    this.HCost = cost;
    this.FCost = cost;
    this.state = state;
    this.pos = pos;
  }
  public void ChangeState(FieldStates state) { this.state = state; }
  public void ChangeFather(Coordinate father) { this.father = father; }
  public void ChangeCosts(Integer G, Integer H) {
    this.GCost = G;
    this.HCost = H;
    this.FCost = this.GCost + this.HCost;
  }
  public FieldStates getState() { return this.state; }
  public Integer getFCost() { return FCost; }
  public Integer getGCost() { return GCost; }
  public Integer getHCost() { return HCost; }
  public Coordinate getPos() { return pos; }
  public Coordinate getFather() { return father; }
}

public class AStar {
  private Node field[][];
  private int actionCost = 10;
  private smartQueue candidates;
  private Coordinate start, finish;
  private HashSet<Coordinate> searched;
  private int sizeX, sizeY;
  private boolean going = true;
  private boolean end = false;
  public AStar(FieldStates[][] field) throws Exception {
    int i, j;
    if (field == null || field.length == 0) {
      throw new Exception("Field was bad");
    }
    sizeX = field.length;
    sizeY = field[0].length;
    this.field = new Node[sizeX][sizeY];
    for (i = 0; i < sizeX; ++i) {
      for (j = 0; j < sizeY; ++j) {
        this.field[i][j] = new Node(null, 0, field[i][j], new Coordinate(i, j));
        if (field[i][j] == FieldStates.Start) {
          start = this.field[i][j].getPos();
        } else if (field[i][j] == FieldStates.Finish) {
          finish = this.field[i][j].getPos();
        }
      }
    }
    searched = new HashSet<Coordinate>();
    if (start == null || finish == null) {
      throw new Exception("No start or finish");
    }
    this.field[start.x][start.y].ChangeState(FieldStates.Start);
    this.field[finish.x][finish.y].ChangeState(FieldStates.Finish);
    this.candidates = new smartQueue();
  }

  public FieldStates[][] getField() {
    FieldStates[][] exhaust = new FieldStates[sizeX][sizeY];
    for (int i = 0; i < sizeX; i++) {
      for (int j = 0; j < sizeY; j++) {
        exhaust[i][j] = this.field[i][j].getState();
      }
    }
    return exhaust;
  }
  private Coordinate makeAnOperation() {
    if (candidates.size() == 0) {
      return null;
    }
    Node candidate = candidates.pollLowest();
    field[candidate.getPos().x][candidate.getPos().y].ChangeCosts(
        candidate.getGCost(), candidate.getHCost());
    field[candidate.getPos().x][candidate.getPos().y].ChangeCosts(
        candidate.getGCost(), candidate.getHCost());
    field[candidate.getPos().x][candidate.getPos().y].ChangeFather(
        candidate.getFather());
    if (field[candidate.getPos().x][candidate.getPos().y].getState() !=
        FieldStates.Finish) {
      field[candidate.getPos().x][candidate.getPos().y].ChangeState(
          FieldStates.Discovered);
    }
    searched.add(candidate.getPos());
    return candidate.getPos();
  }

  private Integer calculateHypothetical(Coordinate point) {
    return (Math.abs(finish.x - point.x) + Math.abs(finish.y - point.y)) *
        actionCost;
  }
  static public void test() {
    smartQueue queue = new smartQueue();
    for (int i = 0; i < 100; ++i) {
      queue.addElemet(
          new Node(null, i, FieldStates.Empty, new Coordinate(i, i)));
    }
    for (int i = 0; i < 100; ++i) {
      System.out.println(queue.peekLowest().getFCost() + " " +
                         queue.pollLowest().getHCost());
    }
  }
  private List<Coordinate> getPath(Coordinate end) {
    List<Coordinate> path = new LinkedList<Coordinate>();
    Coordinate pos = end;
    while (pos != start) {
      path.add(pos);
      pos = field[pos.x][pos.y].getFather();
      if (pos != start) {
        field[pos.x][pos.y].ChangeState(FieldStates.Solution);
      }
    }
    path.add(start);
    return path;
  }
  public void forceStop() { going = false; }

  public int solve() throws Exception {
    Coordinate buffer;
    startSolving(null);
    while (going) {
      buffer = makeAnOperation();
      if (buffer == null) {
        end = true;
        throw new Exception("No solution");
      } else if (buffer.x == finish.x && buffer.y == finish.y) {
        getPath(buffer);
        end = true;
        return 1;
      }

      getPossibilities(buffer);
    }
    return 0;
  }
  public boolean end() { return end; }
  public void startSolving(Integer delay) throws Exception {

    field[start.x][start.y].ChangeCosts(0, calculateHypothetical(start));
    searched.add(field[start.x][start.y].getPos());
    getPossibilities(start);
  }

  public int allowMove() throws Exception {
    Coordinate buffer;
    buffer = makeAnOperation();
    // debug();
    // System.in.read();
    if (buffer == null) {
      throw new Exception("No solution");
    } else if (buffer.x == finish.x && buffer.y == finish.y) {
      getPath(finish);
      return 1;
    }

    getPossibilities(buffer);
    return 0;
  }

  private void getPossibilities(Coordinate pos) {
    int i, j, GCost, HCost;
    Coordinate candidate;
    Node candidateNode;
    for (i = (pos.x - 1); i < pos.x + 2; ++i) {
      for (j = (pos.y - 1); j < pos.y + 2; ++j) {
        if (i >= 0 && i < sizeX && j >= 0 && j < sizeY &&
            field[i][j].getState() != FieldStates.Wall) {
          if (i != pos.x || j != pos.y) {
            candidate = field[i][j].getPos();
            GCost = (int)(Math.sqrt(Math.abs(i - pos.x) + Math.abs(j - pos.y)) *
                          actionCost) +
                    field[pos.x][pos.y].getGCost();
            HCost = calculateHypothetical(candidate);
            if (!searched.contains(candidate)) {
              candidateNode =
                  new Node(pos, GCost + HCost, FieldStates.Known, candidate);
              candidateNode.ChangeCosts(GCost, HCost);
              if (field[i][j].getState() != FieldStates.Finish) {
                field[i][j].ChangeState(FieldStates.Known);
              }
              candidates.addElemet(candidateNode);
            } else {
              if (field[candidate.x][candidate.y].getGCost() >= GCost) {
                field[candidate.x][candidate.y].ChangeCosts(GCost, HCost);
                field[candidate.x][candidate.y].ChangeFather(pos);
              }
            }
          }
        }
      }
    }
  }
}
