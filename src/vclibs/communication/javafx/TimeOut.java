package vclibs.communication.javafx;

import vclibs.communication.Eventos.OnTimeOutListener;
import javafx.concurrent.Task;

//Clase Temporizador, espera un tiempo en un hilo secundario
public class TimeOut extends Task<Integer> {
	
	long time = 0;//Variable de tiempo de espera
	
	//Variables para seleccionar qué imprimir en la Consola
	public boolean idebug = true;
	public boolean edebug = true;
	
	OnTimeOutListener onTOListener;
	
	public void setTimeOutListener(OnTimeOutListener tOListener) {
		onTOListener = tOListener;
	}
	
	/**
	 * Constructor de la clase
	 * @param ms: tiempo de espera solicitado
	 */
	public TimeOut(long ms) {
		time = ms;
		onPreExecute();
	}
	
	//Acciones anteriores al inicio del hilo de ejecusión secundario
	protected void onPreExecute() {
		if(idebug)
			System.out.println("TimeOut - "+"onPreExecute");
		if(onTOListener != null)
			onTOListener.onTimeOutEnabled();
	}

	//Función del hilo de ejecución secundario
	@Override
	protected Integer call() throws Exception {
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

	//Acciones ante cancelación de Actividad del hilo
	@Override
	protected void cancelled() {
		if(idebug)
			System.out.println("TimeOut - "+"onCancelled");
		if(onTOListener != null)
			onTOListener.onTimeOutCancelled();
	}

	//Acciones ante la finalización de acciones del hilo
	@Override
	protected void succeeded() {
		if(idebug)
			System.out.println("TimeOut - "+"onPostExecute");
		if(onTOListener != null)
			onTOListener.onTimeOut();
		super.done();
	}
}