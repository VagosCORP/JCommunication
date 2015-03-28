package vclibs.communication;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import vclibs.communication.Eventos.OnComunicationListener;
import vclibs.communication.Eventos.OnConnectionListener;
import vclibs.communication.javafx.Comunic;
import vclibs.communication.javafx.TimeOut;

public class LayoutController implements Initializable, OnConnectionListener, OnComunicationListener {

	Comunic comunic;
	TimeOut timeOut;
	Thread th, tho;
	
	boolean connected = false;
	public static final int CLIENT = 101;
	public static final int SERVER = 102;
	int tcon = CLIENT;
	public static final String NTn = "#";
	public static final String NTt = "T";
	public static final String TSendHn = "0123456789";
	public static final String TSendHt = "Vagos CORP";
	public static final String CON_GUT = "Conectado";
	public static final String CON_CW = "Conectando...";
	public static final String CON_SW = "Esperando Conexión...";
	public static final String CON_C = "Conectar a Servidor";
	public static final String CON_S = "Iniciar Servidor";
	
	@FXML TextArea State;
	@FXML TextArea RCV;
	@FXML TextField IP;
	@FXML TextField PortClient;
	@FXML TextField PortServer;
	@FXML TextField TSend;
	@FXML CheckBox NT;
	@FXML Label Version;
	@FXML Button ConClient;
	@FXML Button ConServer;
	@FXML Button BorrTx;
	@FXML Button BorrRx;
	@FXML Button Send;
	
	@FXML public void NTClick() {
		if(NT.isSelected()) {
			NT.setText(NTn);
			TSend.setPromptText(TSendHn);
			TSend.setText("");
			State.appendText("Envío de Datos Numéricos(Byte) Habilitador\r\n");
		}else {
			NT.setText(NTt);
			TSend.setPromptText(TSendHt);
			TSend.setText("");
			State.appendText("Envío de Datos Texto Habilitado\r\n");
		}
	}
	
	@FXML public void BorrTxClick() {
		TSend.setText("");
		State.appendText("Información a Enviarse Borrada!\r\n");
	}
	
	@FXML public void BorrRxClick() {
		RCV.setText("");
		State.appendText("Información Recibida Borrada!\r\n");
	}
	
	@FXML public void ConClientClick() {
		tcon = CLIENT;
		connected = !connected;
		ConServer.setDisable(connected);
		IP.setDisable(connected);
		PortClient.setDisable(connected);
		ConClient.setText(CON_CW);
		if(connected) {
			State.appendText("Iniciando Conexión con " + IP.getText() +":" + Integer.parseInt(PortClient.getText()) + "\r\n");
			initClient(IP.getText(), Integer.parseInt(PortClient.getText()));
		}else {
			State.appendText("Cliente Finalizado!\r\n");
			comunic.Detener_Actividad();
		}
	}
	
	@FXML public void ConServerClick() {
		tcon = SERVER;
		connected = !connected;
		ConClient.setDisable(connected);
		PortServer.setDisable(connected);
		ConServer.setText(CON_SW);
		if(connected) {
			State.appendText("Esperando Conexión en Puerto " + Integer.parseInt(PortServer.getText()) + "\r\n");
			initServer(Integer.parseInt(PortServer.getText()));
		}else {
			State.appendText("Servidor Finalizado!\r\n");
			comunic.Detener_Actividad();
		}
	}
	
	@FXML public void SendClick() {
		String txt = TSend.getText();
		if(!txt.equals("")) {
			if(NT.isSelected()) {
				try {
					int val = Integer.parseInt(TSend.getText());
					comunic.enviar(val);
				}catch(Exception e) {
					
				}
			}else {
				comunic.enviar(txt);
			}
		}
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		comunic = new Comunic();
		Version.setText("Vagos CORP Communication v" + comunic.version);
		NT.setSelected(false);
		NT.setText(NTt);
		TSend.setPromptText(TSendHt);
		Send.setDisable(true);
	}
	
	void initClient(String CIP, int CPort) {
		comunic = new Comunic(CIP, CPort);
//		comunic.edebug = false;
		comunic.setConnectionListener(this);
		comunic.setComunicationListener(this);
		th = new Thread(comunic);
		th.setDaemon(true);
		th.start();
	}
	
	void initServer(int SPort) {
		comunic = new Comunic(SPort);
//		comunic.edebug = false;
		comunic.setConnectionListener(this);
		comunic.setComunicationListener(this);
		th = new Thread(comunic);
		th.setDaemon(true);
		th.start();
	}

	@Override
	public void onDataReceived(int nbytes, final String dato, final int[] ndato, byte[] bdato) {
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				RCV.appendText(dato);
//				for(int val:ndato) {
//					RCV.appendText(val + " ");
//	            }
			}
		});
	}

	@Override
	public void onConnectionstablished() {
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				Send.setDisable(false);
				if(tcon == CLIENT) {
					ConClient.setText(CON_GUT);
					State.appendText("Conectado a " + IP.getText() +":" + Integer.parseInt(PortClient.getText()) + "\r\n");
				}else if(tcon == SERVER) {
					ConServer.setText(CON_GUT);
					State.appendText("Conectado en Puerto " + Integer.parseInt(PortServer.getText()) + "\r\n");
				}
			}
		});
	}

	@Override
	public void onConnectionfinished() {
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				Send.setDisable(true);
				State.appendText("Conexión Finalizada!\r\n");
				if(tcon == CLIENT) {
					ConClient.setText(CON_C);
					connected = false;
					ConServer.setDisable(connected);
					IP.setDisable(connected);
					PortClient.setDisable(connected);
				}else if(tcon == SERVER) {
					ConServer.setText(CON_S);
					connected = false;
					ConClient.setDisable(connected);
					PortServer.setDisable(connected);
				}
			}
		});
	}

	@Override
	protected void finalize() throws Throwable {
		comunic.Detener_Actividad();
		super.finalize();
	}
	
	
	
}
