package com.honeywell.mobility.labelprint.sample;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This is the main activity of the LabelPrinterSample which demonstrates
 * label printing using the LabelPrinter class.
 * <p>
 * The sample project contains a printer_profiles.JSON file in the "assets"
 * folder which defines standard settings to communicate with known Honeywell
 * printers. The PRINTERS JSON object contains two printer entries for label
 * printing, PB22_Fingerprint and PB32_Fingerprint. As the name implies, these
 * printer settings are for the PB22 and PB32 printers respectively and the
 * printer command language is Fingerprint. You may add other printer
 * entries to the PRINTERS JSON object. Please refer to the User's Guide for
 * more information.
 * <p>
 * In the UI, please specify either PB22_Fingerprint or PB32_Fingerprint in
 * the Printer ID input field. The Bluetooth MAC Address field should contain
 * the Bluetooth MAC Address of your printer. The PB22_Fingerprint settings
 * allow you to print 2" labels and the PB32_Fingerprint settings print 3"
 * labels.
 * <p>
 * Press the Print Item Label button to print an item label with item barcode
 * or Press the Print URL Label button to print a QRCode encoded URL.
 */
public class LabelPrintActivity extends Activity {
	public static final String PRINTER_ID_KEY = "PrinterID";
	public static final String BT_MAC_ADDRESS_KEY = "BtMacAddress";
	public static final String PRINTER_SETTINGS_KEY = "PrinterSettings";
	private EditText editPrinterID;
	private EditText editMacAddr;
	private String jsonCmdAttribStr = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_label_print);
		editPrinterID = (EditText) findViewById(R.id.editPrinterID);
		// Set a default Printer ID
		editPrinterID.setText("PB32_Fingerprint");

		editMacAddr = (EditText) findViewById(R.id.editMacAddr);
		// Set a default Mac Address
		editMacAddr.setText("00:06:66:02:C4:3A");

		readAssetFiles();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.label_print, menu);
		return true;
	}

	/**
	 * Invoked when the Print Item Label button is clicked.
	 * @param view The view that was clicked.
	 */
	public void onPrintItemLabelButtonClicked (View view)
	{
		String sPrinterID = editPrinterID.getText().toString();
		String sMacAddr = formatMacAddress(editMacAddr.getText().toString());

		Intent itemIntent = new Intent (this, PrintItemLabelActivity.class);
		itemIntent.putExtra(PRINTER_ID_KEY, sPrinterID);
		itemIntent.putExtra(BT_MAC_ADDRESS_KEY, sMacAddr);
		itemIntent.putExtra(PRINTER_SETTINGS_KEY, jsonCmdAttribStr);
		startActivity(itemIntent);
	}

	/**
	 * Invoked when the Print URL Label button is clicked.
	 * @param view The view that was clicked.
	 */
	public void onPrintURLLabelButtonClicked (View view)
	{
		String sPrinterID = editPrinterID.getText().toString();
		String sMacAddr = formatMacAddress(editMacAddr.getText().toString());

		Intent urlIntent = new Intent (this, PrintURLLabelActivity.class);
		urlIntent.putExtra(PRINTER_ID_KEY, sPrinterID);
		urlIntent.putExtra(BT_MAC_ADDRESS_KEY, sMacAddr);
		urlIntent.putExtra(PRINTER_SETTINGS_KEY, jsonCmdAttribStr);
		startActivity(urlIntent);
	}

	/**
	 * If the specified MAC address contains 12 characters without the ":"
	 * delimiters, it adds the delimiters; otherwise, it returns the original
	 * string.
	 * @param aMacAddress A string containing the MAC address.
	 * @return a formatted string or the original string.
	 */
	public static String formatMacAddress (String aMacAddress)
	{
		if (aMacAddress != null && aMacAddress.contains(":") == false &&
			aMacAddress.length() == 12)
		{
			// If the MAC address only contains hex digits without the
			// ":" delimiter, then add ":" to the MAC address string.
			char[] cAddr = new char[17];

			for (int i=0, j=0; i < 12; i += 2)
			{
				aMacAddress.getChars(i, i+2, cAddr, j);
				j += 2;
				if (j < 17)
				{
					cAddr[j++] = ':';
				}
			}

			return new String(cAddr);
		}
		else
		{
			return aMacAddress;
		}
	}

	private void readAssetFiles()
	{
		InputStream input = null;
		ByteArrayOutputStream output = null;
		AssetManager assetManager = getAssets();
		Toast toast;

		try
		{
			input = assetManager.open("printer_profiles.JSON");
			output = new ByteArrayOutputStream(8000);

			byte[] buf = new byte[1024];
			int len;
			while ((len = input.read(buf)) > 0)
			{
				output.write(buf, 0, len);
			}
			input.close();
			input = null;

			output.flush();
			output.close();
			jsonCmdAttribStr = output.toString();
			output = null;
		}
		catch (Exception ex)
		{
			toast = Toast.makeText(this, "Error reading asset file: printer_profiles.JSON", Toast.LENGTH_SHORT);
			toast.show();
		}
		finally
		{
			try
			{
				if (input != null)
				{
					input.close();
					input = null;
				}

				if (output != null)
				{
					output.close();
					output = null;
				}
			}
			catch (IOException e){}
		}
	}
}
