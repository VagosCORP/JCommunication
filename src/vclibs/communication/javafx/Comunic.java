package vclibs.communication.javafx;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.concurrent.Task;
import vclibs.communication.Inf;
import vclibs.communication.Eventos.OnComunicationListener;
import vclibs.communication.Eventos.OnConnectionListener;

//Clase de comunicación de Red para la Computadora
public class Comunic extends Task<Integer> {

	//Constantes deribadas de la Clase Inf
	public final String version = Inf.version;
	public final int NULL = Inf.NULL;// estado
	public final int WAITING = Inf.WAITING;// estado
	public final int CONNECTED = Inf.CONNECTED;// estado
	public final int CLIENT = Inf.CLIENT;// tcon
	public final int SERVER = Inf.SERVER;// tcon
	
	//Constantes para reportes de estado
	public final String EN_ESPERA = "EN_ESPERA";//{ 1 };
	public final String CONECTADO = "CONECTADO";//{ 2 };
	final String IO_EXCEPTION = "IO_EXCEPTION";//{ 3 };
	final String CONEXION_PERDIDA = "CONEXION PERDIDA";//{ 4 };
	public final String DATO_RECIBIDO = "DATO_RECIBIDO";//{ 7 };
	
	InetSocketAddress isa;//Dirección a la cual conectarse
	int sPort = 2000;//Puerto de Servidor, valor por defecto: 2000
	Socket socket;//Medio de Conexión de Red
	ServerSocket serverSocket;//Medio de Conexión del Servidor
	DataInputStream inputSt;//Flujo de datos de entrada
	DataOutputStream outputSt;//Flujo de datos de salida
	public int tcon = NULL;//Tipo de conexión actual
	boolean conectado = false;
	public int estado = NULL;//Estado actual
	
	//Variables para seleccionar qué imprimir en la Consola
	public boolean debug = true;
	public boolean idebug = true;
	public boolean edebug = true;
	
	public boolean Flag_LowLevel = true;

	//Eventos usados según el caso
	OnConnectionListener onConnListener;
	OnComunicationListener onCOMListener;

	/**
	 * Definir acciones ante eventos de conexión
	 * @param connListener: Instancia del Evento
	 */
	public void setConnectionListener(OnConnectionListener connListener) {
		onConnListener = connListener;
	}
	
	/**
	 * Definir acciones ante eventos de comunicación
	 * @param comListener: Instancia del Evento
	 */
	public void setComunicationListener(OnComunicationListener comListener) {
		onCOMListener = comListener;
	}

	/**
	 * Impresión de información de depuración
	 * @param text: Mensaje a imprimir
	 */
	private void wlog(String text) {
		if(debug)
			Inf.println(tcon, text);
	}
	
	/**
	 * Impresión de información referente al estado Actual
	 * @param text
	 */
	private void ilog(String text) {
		if(idebug)
			Inf.println(tcon, text);
	}

	//Constructor simple de la clase, solo inicialización de variables
	public Comunic() {
		estado = NULL;
	}
	
	/**
	 * Constructor de la clase para modo Cliente
	 * @param ip: Dirección IP del servidor al cual conectarse
	 * @param port: Puerto del Servidor al cual conectarse
	 */
	public Comunic(String ip, int port) {
		estado = NULL;
		tcon = CLIENT;
		isa = new InetSocketAddress(ip, port);
		onPreExecute();
	}
	
	/**
	 * Constructor de la clase para modo Servidor
	 * @param port: Puerto a la espera de conexión
	 */
	public Comunic(int port) {
		estado = NULL;
		tcon = SERVER;
		sPort = port;
		onPreExecute();
	}

	/**
	 * Función de envio de Texto
	 * @param dato
	 */
	public void enviar(String dato) {
		try {
			if (estado == CONNECTED)
				outputSt.writeBytes(dato);
		} catch (IOException e) {
			wlog(e.getMessage());
			if(edebug)
				e.printStackTrace();
		}
	}

	/**
	 * función de envio numérico, 1 Byte (rango de 0 a 255)
	 * @param dato
	 */
	public void enviar(int dato) {
		try {
			if (estado == CONNECTED)
				outputSt.writeByte(dato);
		} catch (IOException e) {
			wlog(e.getMessage());
			if(edebug)
				e.printStackTrace();
		}
	}

	//Función de finalización de Conexión
	public void Cortar_Conexion() {
		try {	
			if (estado == CONNECTED && socket != null) {
				socket.close();
				cancel(true);// socket = null;
			}
		} catch (IOException e) {
			wlog(e.getMessage());
			if(edebug)
				e.printStackTrace();
		}
	}

	//Función de finalización de Espera a conexión del servidor
	public void Detener_Espera() {
		try {
			if (estado == WAITING) {
				// cancel(true);
				if (serverSocket != null)
					serverSocket.close();
				ilog(Inf.ESPERA_DETENIDA);
			}
		} catch (IOException e) {
			wlog(e.getMessage());
			if(edebug)
				e.printStackTrace();
		}
	}
	
	//Función de finalización de actividad actual 
	public void Detener_Actividad() {
		Cortar_Conexion();
		Detener_Espera();
	}

	//Acciones anteriores al inicio del hilo de ejecusión secundario
	protected void onPreExecute() {
		estado = NULL;
		socket = null;
		serverSocket = null;
		conectado = false;
	}

	//Función del hilo de ejecución secundario
	@Override
	protected Integer call() throws Exception {
		try {
			if (tcon == CLIENT) {
				socket = new Socket();
				if (socket != null) {
					socket.connect(isa,7000);//reintentar por 7 segundos
				} else
					socket = null;
			} else if (tcon == SERVER) {
				serverSocket = new ServerSocket(sPort);
				if (serverSocket != null) {
					updateMessage(EN_ESPERA);
					socket = serverSocket.accept();
					serverSocket.close();
					serverSocket = null;
				} else
					socket = null;
			}
			if (socket != null && socket.isConnected()) {
				inputSt = new DataInputStream(socket.getInputStream());
				outputSt = new DataOutputStream(socket.getOutputStream());
				conectado = true;
				updateMessage(CONECTADO);
				while (socket.isConnected() && conectado && !isCancelled()) {
					byte[] buffer = new byte[1024];
					int len = inputSt.read(buffer);
					if (len != -1) {
						String rcv = new String(buffer, 0, len);
						updateMessage(DATO_RECIBIDO);
						updateMessage(rcv);
					}else
						updateMessage(CONEXION_PERDIDA);
				}
				conectado = false;
				inputSt.close();
				outputSt.close();
				if (socket != null)
					socket.close();
			}
		} catch (IOException e) {
			wlog(Inf.IO_EXCEPTION);
			updateMessage(IO_EXCEPTION);
			wlog(e.getMessage());
			if(edebug)
				e.printStackTrace();
		}
		return null;
	}

	//Reporte de estado al hilo de ejecución principal
	@Override
	protected void updateMessage(String message) {
		if (message == EN_ESPERA) {
			estado = WAITING;
			ilog(Inf.EN_ESPERA);
		} else if (message == DATO_RECIBIDO) {
			
		} else if (message == CONECTADO) {
			estado = CONNECTED;
			if (onConnListener != null)
				onConnListener.onConnectionstablished();
			ilog(Inf.CONECTADO);
		} else if (message == IO_EXCEPTION) {
//			wlog(Inf.IO_EXCEPTION);
			estado = NULL;
		} else if (message == CONEXION_PERDIDA) {
			wlog(Inf.CONEXION_PERDIDA);
			Cortar_Conexion();
		} else {
			if (onCOMListener != null)
				onCOMListener.onDataReceived(message, null);
			wlog(Inf.DATO_RECIBIDOx + message);
		}
		super.updateMessage(message);
	}

	//Acciones ante cancelación de Actividad del hilo
	@Override
	protected void cancelled() {
		wlog(Inf.ON_CANCELLED);
		succeeded();
		super.cancelled();
	}
	
	//Acciones ante la finalización de acciones del hilo
	@Override
	protected void succeeded() {
		estado = NULL;
		if (onConnListener != null)
			onConnListener.onConnectionfinished();
		ilog(Inf.ON_POSTEXEC);
		super.succeeded();
	}
}