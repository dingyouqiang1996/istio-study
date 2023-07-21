
	
import com.chilkatsoft.CkHttp;
import com.chilkatsoft.CkTask;

public class AsyncHttp {
	
  static {
    try {
    	
    	System.loadLibrary("chilkat");
    	
    } catch (UnsatisfiedLinkError e) {
      System.err.println("Native code library failed to load.\n" + e);
      System.exit(1);
    }
  }


	
  public static void main(String argv[])
  	{
    CkHttp http = new CkHttp();
    
    // Install an event callback handler to get progress events.
    MyHttpProgress myProgress = new MyHttpProgress();
    http.put_EventCallbackObject(myProgress);
    
    boolean success;

    //  Any string unlocks the component for the 1st 30-days.
    success = http.UnlockComponent("Anything for 30-day trial");
    if (success != true) {
        System.out.println(http.lastErrorText());
        return;
    }

    //  Download a file at a URL.
    CkTask task = http.DownloadAsync("http://www.chilkatsoft.com/download/9.5.0.51/ChilkatDotNet45-9.5.0-x64.zip","ChilkatDotNet45-9.5.0-x64.zip");
    if (success != true) {
        System.out.println(http.lastErrorText());
        return;
    }
    
    task.put_UserData("chilkatDotNet45");
    
    if (!task.Run()) {
        System.out.println(task.lastErrorText());
        return;
	}
  
    System.out.println("OK, task is running...");
    
    // Wait a max of 10 seconds for it to finish.
    success = task.Wait(10000);
    
    // What is the task status?
    System.out.println("task status = " + task.status());
  	}
  }

