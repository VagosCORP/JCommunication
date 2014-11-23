package vclibs.communication.deprecated;

import javax.swing.SwingWorker;

import vclibs.communication.Eventos.OnTimeOutListener;

// TODO: Auto-generated Javadoc
/**
 * The Class JTimeOut.
 */
public class JTimeOuts extends SwingWorker<Integer, Void> {
	
	/** The time. */
	long time = 0;
	
	/** The on to listener. */
	OnTimeOutListener onTOListener;
	
//	/**
//	 * The listener interface for receiving onTimeOut events.
//	 * The class that is interested in processing a onTimeOut
//	 * event implements this interface, and the object created
//	 * with that class is registered with a component using the
//	 * component's <code>addOnTimeOutListener<code> method. When
//	 * the onTimeOut event occurs, that object's appropriate
//	 * method is invoked.
//	 *
//	 * @see OnTimeOutEvent
//	 */
//	public interface OnTimeOutListener {
//		
//		/**
//		 * On time out enabled.
//		 */
//		public void onTimeOutEnabled();
//		
//		/**
//		 * On time out cancelled.
//		 */
//		public void onTimeOutCancelled();
//		
//		/**
//		 * On time out.
//		 */
//		public void onTimeOut();
//	}
	
	/**
	 * Sets the time out listener.
	 *
	 * @param tOListener the new time out listener
	 */
	public void setTimeOutListener(OnTimeOutListener tOListener) {
		onTOListener = tOListener;
	}
	
	/**
	 * Instantiates a new j time out.
	 *
	 * @param ms the ms
	 */
	public JTimeOuts(long ms) {
		time = ms;
		onPreExecute();
	}
	
	/**
	 * On pre execute.
	 */
	protected void onPreExecute() {
		System.out.println("TimeOut - "+"onPreExecute");
		if(onTOListener != null)
			onTOListener.onTimeOutEnabled();
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Integer doInBackground() {
		System.out.println("TimeOut - "+"doInBackground");
		long ms = time/10;
		try {
			for(int i=0; !isCancelled() && i < ms; i++) {
				Thread.sleep(10);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return 1;
	}

	/**
	 * On cancelled.
	 */
	protected void onCancelled() {
		System.out.println("TimeOut - "+"onCancelled");
		if(onTOListener != null)
			onTOListener.onTimeOutCancelled();
	}
	
	/**
	 * On post execute.
	 */
	protected void onPostExecute() {
		System.out.println("TimeOut - "+"onPostExecute");
		if(onTOListener != null)
			onTOListener.onTimeOut();
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		if(!isCancelled())
			onPostExecute();
		else
			onCancelled();
		super.done();
	}
	
}
