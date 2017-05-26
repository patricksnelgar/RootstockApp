package com.honeywell.mobility.labelprint.sample;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.honeywell.mobility.print.LabelPrinter;
import com.honeywell.mobility.print.LabelPrinterException;
import com.honeywell.mobility.print.PrintProgressEvent;
import com.honeywell.mobility.print.PrintProgressListener;

/**
 * This activity allows you to print a label containing the QRCode encoded URL
 * and two text lines.
 * <p>
 * The label printing logic is implemented in a subclass of AsyncTask so
 * that it is executed in a separate thread. This sample opens and closes
 * the printer connection per print. For real world applications, you may
 * print multiple labels before closing the connection.
 * <p>
 * The printing progress will be displayed in the Progress and Status text box.
 */
public class PrintURLLabelActivity extends Activity {
	private EditText editURL;
	private EditText editLine1;
	private EditText editLine2;
	private Button buttonPrint;
	private TextView textMsg;
	private String printerID = null;
	private String macAddress = null;
	private String jsonCmdAttribStr = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_print_url_label);

		editURL = (EditText) findViewById(R.id.editURL);
		// Sets a default URL.
		editURL.setText("http://www.honeywellaidc.com");

		editLine1 = (EditText) findViewById(R.id.editLine1);
		// Sets a default line 1 text.
		editLine1.setText("Scan for more info");

		editLine2 = (EditText) findViewById(R.id.editLine2);
		// Sets a default line 2 text.
		editLine2.setText("www.honeywellaidc.com");

		textMsg = (TextView) findViewById(R.id.textMsg);
		buttonPrint = (Button) findViewById(R.id.buttonPrint);

		// Gets the employee ID and work order number passed to this activity.
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			printerID = extras.getString(LabelPrintActivity.PRINTER_ID_KEY);
			macAddress = extras.getString(LabelPrintActivity.BT_MAC_ADDRESS_KEY);
			jsonCmdAttribStr = extras.getString(LabelPrintActivity.PRINTER_SETTINGS_KEY);
		} //endif (extras != null)

		if (printerID == null || printerID.length() == 0)
		{
			textMsg.append("Error: printer ID is null or empty\n");
			buttonPrint.setEnabled(false);
		}

		if (macAddress == null || macAddress.length() == 0)
		{
			textMsg.append("Error: printer BT MAC address is null or empty\n");
			buttonPrint.setEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.print_urllabel, menu);
		return true;
	}

	/**
	 * Invoked when the Print button is clicked.
	 * @param view The view that was clicked
	 */
	public void onPrintButtonClicked(View view)
	{
		// Create a PrintTask to do printing on a separate thread.
		PrintTask task = new PrintTask();

		// Executes PrintTask with the specified parameter which is passed
		// to the PrintTask.doInBackground method.
		task.execute(printerID, macAddress);
	}

	/**
	 * This class demonstrates printing in a background thread and updates
	 * the UI in the UI thread.
	 */
	public class PrintTask extends AsyncTask<String, Integer, String> {
		private static final String PROGRESS_CANCEL_MSG = "Printing cancelled\n";
		private static final String PROGRESS_COMPLETE_MSG = "Printing completed\n";
		private static final String PROGRESS_ENDDOC_MSG = "End of label printing\n";
		private static final String PROGRESS_FINISHED_MSG = "Printer connection closed\n";
		private static final String PROGRESS_NONE_MSG = "Unknown progress message\n";
		private static final String PROGRESS_STARTDOC_MSG = "Start printing label\n";
		private String sURL, sLine1, sLine2;

		/**
		 * Runs on the UI thread before doInBackground(Params...).
		 */
		@Override
		protected void onPreExecute()
		{
			// Clears the Progress and Status text box.
			textMsg.setText("");
			sURL = editURL.getText().toString();
			sLine1 = editLine1.getText().toString();
			sLine2 = editLine2.getText().toString();

			// Disables the Print button.
			buttonPrint.setEnabled(false);
		}

		/**
		 * This method runs on a background thread. The specified parameters
		 * are the parameters passed to the execute method by the caller of
		 * this task. This method can call publishProgress to publish updates
		 * on the UI thread.
		 */
		@Override
		protected String doInBackground(String... args)
		{
			LabelPrinter lp = null;
			String sResult = null;
			String sPrinterID = args[0];
			String sPrinterURI = "bt://" + args[1];

			LabelPrinter.ExtraSettings exSettings = new LabelPrinter.ExtraSettings();
			exSettings.setContext(PrintURLLabelActivity.this);

			try
			{
				lp = new LabelPrinter(
						jsonCmdAttribStr,
						sPrinterID,
						sPrinterURI,
						exSettings);

				// Registers to listen for the print progress events.
				lp.addPrintProgressListener(new PrintProgressListener() {
					@Override
					public void receivedStatus(PrintProgressEvent aEvent)
					{
						// Publishes updates on the UI thread.
						publishProgress(aEvent.getMessageType());
					}
				});

				// A retry sequence in case the bluetooth socket is temporarily not ready
				int numtries = 0;
				int maxretry = 2;
				while(numtries < maxretry)
				{
					try
					{
						lp.connect();  // Connects to the printer
						break;
					}
					catch (LabelPrinterException ex)
					{
						numtries++;
						Thread.sleep(1000);
					}
				}
				if (numtries == maxretry) lp.connect();//Final retry

				// Sets up the variable dictionary.
				LabelPrinter.VarDictionary varDataDict = new LabelPrinter.VarDictionary();
				varDataDict.put("URL", sURL);
				varDataDict.put("TextLine1", sLine1);
				varDataDict.put("TextLine2", sLine2);

				// Prints the URL_QRLabel as defined in the printer_profiles.JSON file.
				lp.writeLabel("URL_QRLabel", varDataDict);

				sResult = "Number of bytes sent to printer: " + lp.getBytesWritten();
			}
			catch (LabelPrinterException ex)
			{
				sResult = "LabelPrinterException: " + ex.getMessage();
			}
			catch (Exception ex)
			{
				if (ex.getMessage() != null)
					sResult = "Unexpected exception: " + ex.getMessage();
				else
					sResult = "Unexpected exception.";
			}
			finally
			{
				if (lp != null)
				{
					try
					{
						// Notes: To ensure the data is transmitted to the printer
						// before the connection is closed, both PB22_Fingerprint and
						// PB32_Fingerprint printer entries specify a PreCloseDelay setting
						// in the printer_profiles.JSON file included with this sample.
						lp.disconnect();  // Disconnects from the printer
						lp.close();  // Releases resources
					}
					catch (Exception ex) {}
				}
			}

			// The result string will be passed to the onPostExecute method
			// for display in the the Progress and Status text box.
			return sResult;
		}

		/**
		 * Runs on the UI thread after publishProgress is invoked. The
		 * specified values are the values passed to publishProgress.
		 */
		@Override
		protected void onProgressUpdate(Integer... values)
		{
			// Access the values array.
			int progress = values[0];

			switch (progress)
			{
			case PrintProgressEvent.MessageTypes.CANCEL:
				textMsg.append(PROGRESS_CANCEL_MSG);
				break;
			case PrintProgressEvent.MessageTypes.COMPLETE:
				textMsg.append(PROGRESS_COMPLETE_MSG);
				break;
			case PrintProgressEvent.MessageTypes.ENDDOC:
				textMsg.append(PROGRESS_ENDDOC_MSG);
				break;
			case PrintProgressEvent.MessageTypes.FINISHED:
				textMsg.append(PROGRESS_FINISHED_MSG);
				break;
			case PrintProgressEvent.MessageTypes.STARTDOC:
				textMsg.append(PROGRESS_STARTDOC_MSG);
				break;
			default:
				textMsg.append(PROGRESS_NONE_MSG);
				break;
			}
		}

		/**
		 * Runs on the UI thread after doInBackground method. The specified
		 * result parameter is the value returned by doInBackground.
		 */
		@Override
		protected void onPostExecute(String result)
		{
			// Displays the result (number of bytes sent to the printer or
			// exception message) in the Progress and Status text box.
			if (result != null)
			{
				textMsg.append(result);
			}

			// Enables the Print button.
			buttonPrint.setEnabled(true);
		}
	} //endofclass PrintTask
}
