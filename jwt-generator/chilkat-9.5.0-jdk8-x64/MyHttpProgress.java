
import com.chilkatsoft.CkHttpProgress;
import com.chilkatsoft.CkTask;


public class MyHttpProgress extends CkHttpProgress 
{	
  public boolean AbortCheck()
  	{
      System.out.println("AbortCheck");
      // Return true to abort, false to allow the method to continue.
      return false;
  	}
  	
  // pctDone is a value from 0 to 100
  // (it is actually value from 0 to the PercentDoneScale property setting)
  public boolean PercentDone(int pctDone)
  {
    System.out.println(pctDone);
    // Return true to abort, false to allow the method to continue.
    // Note: A PercentDone event is the equivalent of an AbortCheck.  
    // When PercentDone events are frequently firing, AbortCheck events are suppressed.
    // AbortCheck events will fire when the time between PercentDone events is longer 
    // than the HeartbeatMs property setting.
    return false;
  }
  
  public void ProgressInfo(String name, String value)
  {
    System.out.println(name + ": " + value);
  }
  
  public void TaskCompleted(CkTask task)
  {
     System.out.println("task completed!");
  }
  
  }
