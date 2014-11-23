package vclibs.communication.java;

import javax.swing.SwingWorker;

import vclibs.communication.Eventos.OnTimeOutListener;

public class TimeOut extends SwingWorker<Integer, Void> {
	
	long time = 0;
	public boolean idebug = true;
	public boolean edebug = true;
	
	OnTimeOutListener onTOListener;
	
	public void setTimeOutListener(OnTimeOutListener tOListener) {
		onTOListener = tOListener;
	}
	
	public TimeOut(long ms) {
		time = ms;
		onPreExecute();
	}
	
	protected void onPreExecute() {
		if(idebug)
			System.out.println("TimeOut - "+"onPreExecute");
		if(onTOListener != null)
			onTOListener.onTimeOutEnabled();
	}

	@Override
	protected Integer doInBackground() {
		if(idebug)
			if(idebug)
				System.out.println("TimeOut - "+"doInBackground");
		long ms = time/10;
		try {
			for(int i=0; !isCancelled() && i < ms; i++) {
				Thread.sleep(10);
			}
		} catch (InterruptedException e) {
			if(edebug)
				e.printStackTrace();
		}
		return 1;
	}

	protected void onCancelled() {
		if(idebug)
			System.out.println("TimeOut - "+"onCancelled");
		if(onTOListener != null)
			onTOListener.onTimeOutCancelled();
	}
	
	protected void onPostExecute() {
		if(idebug)
			System.out.println("TimeOut - "+"onPostExecute");
		if(onTOListener != null)
			onTOListener.onTimeOut();
	}

	@Override
	protected void done() {
		if(!isCancelled())
			onPostExecute();
		else
			onCancelled();
		super.done();
	}
}
