package vclibs.communication.java;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import javax.swing.SwingWorker;

import vclibs.communication.Inf;
import vclibs.communication.Eventos.OnComunicationListener;
import vclibs.communication.Eventos.OnConnectionListener;

public class Comunic extends SwingWorker<Integer, byte[]> {

	public final String version = Inf.version;
	public final int NULL = Inf.NULL;// estado
	public final int WAITING = Inf.WAITING;// estado
	public final int CONNECTED = Inf.CONNECTED;// estado
	public final int CLIENT = Inf.CLIENT;// tcon
	public final int SERVER = Inf.SERVER;// tcon
	final byte[] EN_ESPERA = { 1 };
	final byte[] CONECTADO = { 2 };
	final byte[] IO_EXCEPTION = { 3 };
	final byte[] CONEXION_PERDIDA = { 4 };
	final byte[] DATO_RECIBIDO = { 7 };
	InetSocketAddress isa;
	int sPort = 2000;
	Socket socket;
	ServerSocket serverSocket;
	DataInputStream inputSt;
	DataOutputStream outputSt;
	public int tcon = NULL;
	boolean conectado = false;
	public int estado = NULL;
	
	public boolean debug = true;
	public boolean idebug = true;
	public boolean edebug = true;
	
	public boolean Flag_LowLevel = true;
	
	OnConnectionListener onConnListener;
	OnComunicationListener onCOMListener;

	public void setConnectionListener(OnConnectionListener connListener) {
		onConnListener = connListener;
	}
	public void setComunicationListener(OnComunicationListener comListener) {
		onCOMListener = comListener;
	}

	private void wlog(String text) {
		if(debug)
			Inf.println(tcon, text);
	}
	
	private void ilog(String text) {
		if(idebug)
			Inf.println(tcon, text);
	}

	public Comunic() {
		estado = NULL;
	}
	
	public Comunic(int port) {
		estado = NULL;
		tcon = SERVER;
		sPort = port;
		onPreExecute();
	}

	public Comunic(String ip, int port) {
		estado = NULL;
		tcon = CLIENT;
		isa = new InetSocketAddress(ip, port);
		onPreExecute();
	}

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
	
	public void Detener_Actividad() {
		Cortar_Conexion();
		Detener_Espera();
	}

	protected void onPreExecute() {
		estado = NULL;
		socket = null;
		serverSocket = null;
		conectado = false;
	}

	@Override
	protected Integer doInBackground() {
		try {
			if (tcon == CLIENT) {
				socket = new Socket();
				if (socket != null) {
					socket.connect(isa,7000);
				} else
					socket = null;
			} else if (tcon == SERVER) {
				serverSocket = new ServerSocket(sPort);
				if (serverSocket != null) {
					publish(EN_ESPERA);
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
				publish(CONECTADO);
				while (socket.isConnected() && conectado && !isCancelled()) {
					byte[] buffer = new byte[1024];
					int len = inputSt.read(buffer);
					if (len != -1) {
						byte[] blen = (len + "").getBytes();
						publish(DATO_RECIBIDO, blen, buffer);
					}else
						publish(CONEXION_PERDIDA);
				}
				conectado = false;
				inputSt.close();
				outputSt.close();
				if (socket != null)
					socket.close();
			}
		} catch (IOException e) {
			wlog(Inf.IO_EXCEPTION);
			publish(IO_EXCEPTION);
			wlog(e.getMessage());
			if(edebug)
				e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void process(List<byte[]> values) {
		byte[] orden = values.get(0);
		if (orden == EN_ESPERA) {
			estado = WAITING;
			ilog(Inf.EN_ESPERA);
		} else if (orden == DATO_RECIBIDO) {
			int len = Integer.parseInt(new String(values.get(1)));
			byte[] buffer = values.get(2);
			String rcv = new String(buffer, 0, len);
			int[] nrcv = new int[len];
			if(Flag_LowLevel) {
				for(int i = 0; i < len; i++) {
					nrcv[i] = 0xFF & buffer[i];
				}
			}
			if (onCOMListener != null)
				onCOMListener.onDataReceived(len, rcv, nrcv, buffer);
			wlog(Inf.DATO_RECIBIDOx + rcv);
		} else if (orden == CONECTADO) {
			estado = CONNECTED;
			if (onConnListener != null)
				onConnListener.onConnectionstablished();
			ilog(Inf.CONECTADO);
		} else if (orden == IO_EXCEPTION) {
//			wlog(Inf.IO_EXCEPTION);
			estado = NULL;
		} else if (orden == IO_EXCEPTION) {
			wlog(Inf.CONEXION_PERDIDA);
			Cortar_Conexion();
		}
		super.process(values);
	}

	@Override
	protected void done() {
		estado = NULL;
		if (onConnListener != null)
			onConnListener.onConnectionfinished();
		ilog(Inf.ON_POSTEXEC);
		super.done();
	}
}
