public class AThread extends Thread
{
  private AStar aStar;
  private boolean solved = false;
  private boolean started = false;
  public AThread (FieldStates[][] field) throws Exception
  {
    aStar = new AStar (field);
  }

  public void
  solve ()
  {
    try
      {
        aStar.solve ();
        return;
      }
    catch (Exception e)
      {
        System.err.println (e.getMessage ());
        return;
      }
  }
  public void
  run ()
  {
  }
  public void
  forceStop ()
  {
    aStar.forceStop ();
  }
  public boolean
  allowMove ()
  {
    if (!solved)
      {
        try
          {
            if (!started)
              {
                aStar.startSolving (null);
              }
            if (aStar.allowMove () == 1)
              {
                solved = true;
              }
          }
        catch (Exception e)
          {
            System.err.println (e.getMessage ());
          }
      }
    return solved;
  }

  public FieldStates[][] getField () { return aStar.getField (); }
}