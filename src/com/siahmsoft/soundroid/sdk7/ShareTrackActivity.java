package com.siahmsoft.soundroid.sdk7;


import com.siahmsoft.soundroid.sdk7.async.ResultListener;
import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksManager;
import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksStore.Track;
import com.siahmsoft.soundroid.sdk7.util.ContactAccessor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Esta pantalla tiene que mostrar un listado de emails con los que compartir el tema.
 *
 * Tiene:
 *  - Botón para elegir contactos del teléfono
 *  - Botón para añadir un contacto a mano
 *  - Botón para eliminar contacto
 *  - Botón de compartir
 *
 * @author Antonio
 *
 */
public class ShareTrackActivity extends ListActivity {
    // Request code for the contact picker activity
    private static final int PICK_CONTACT_REQUEST = 1;

    // Request code for the contact picker activity
    private static final int PICK_EMAIL_REQUEST = 2;

    private final int DIALOGO_SELECCION_ORIGEN_MAIL = 0;
    private final int DIALOGO_MAIL_MANUAL = 1;
    private final int DIALOGO_MAIL_AGENDA = 2;

    private static final int CMD_DELETE = 4;

    private String itemSelected;

    private Uri uriTrack;
    private ProgressDialog progressDialog;
    private ArrayList<String> mails;

    private ArrayAdapter<String> adapter;

    private class ShareTrack extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Track track = TracksManager.findTrack(getContentResolver(), uriTrack);

            Soundroid.getSc().shareTrack(String.valueOf(track.getmIdTrack()), mails, new ResultListener<Track>() {

                @Override
                public void onTrackReceived(Track track) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onSuccess(Track result) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onError(Exception e) {
                    // TODO Auto-generated method stub

                }
            });

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uriTrack = getIntent().getData();

        mails = new ArrayList<String>();

        setContentView(R.layout.share_track_activity);

        adapter =  new ArrayAdapter<String>(ShareTrackActivity.this, R.layout.row, android.R.id.text1, mails);
        //mails.add("yakura.kim@gmail.com");

        View header = getLayoutInflater().inflate(R.layout.share_track_list_header, null);

        ImageButton addMailButton = (ImageButton) header.findViewById(R.id.addMailButton);
        // addMailButton.setImageResource(R.drawable.paperclip);

        ImageButton shareTrackButton = (ImageButton) header.findViewById(R.id.shareTrackButton);
        // shareTrackButton.setImageResource(R.drawable.bullhorn);

        addMailButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //Aquí debería mostrar un diálogo para seleccionar si se quiere introducir un mail manualmente
                // o seleccionarlo desde la BBDD de contactos del móvil
                showDialog(DIALOGO_SELECCION_ORIGEN_MAIL);
            }
        });

        shareTrackButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mails != null && mails.size() > 0) {
                    new ShareTrack().execute();
                } else {
                    Toast.makeText(ShareTrackActivity.this, "You must add at least one mail", Toast.LENGTH_LONG).show();
                }

                new ShareTrack().execute();
            }
        });

        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                HeaderViewListAdapter sa = (HeaderViewListAdapter) arg0.getAdapter();
                String mail = (String) sa.getItem(arg2);
                itemSelected = mail;

                return false;
            }

        });

        registerForContextMenu(getListView());

        getListView().addHeaderView(header, null, false);

        setListAdapter(adapter);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        View layout;
        LayoutInflater inflater = LayoutInflater.from(this);
        AlertDialog alertDialog;

        switch (id) {
            case DIALOGO_SELECCION_ORIGEN_MAIL:
                layout = inflater.inflate(R.layout.selection_mail_dialog, null);

                final RadioButton radioManual = (RadioButton) layout.findViewById(R.id.radioManual);
                final RadioButton radioAgenda = (RadioButton) layout.findViewById(R.id.radioAgenda);

                alertDialog = new AlertDialog.Builder(ShareTrackActivity.this)
                .setView(layout)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Select mail´s source")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked OK so do some stuff */

                        if(radioManual.isChecked()){
                            //Lanzar diálogo para añadir mail manualmente
                            addMailManual();
                        }else if(radioAgenda.isChecked()){
                            //Lanzar Activity de Android para recuperar los contactos de la agenda
                            addMailAgenda();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* User clicked cancel so do some stuff */
                    }
                })
                .create();

                return alertDialog;

            case DIALOGO_MAIL_MANUAL:
                layout = inflater.inflate(R.layout.manual_mail_dialog, null);

                final EditText mailField = (EditText) layout.findViewById(R.id.campoMail);
                mailField.setInputType(InputType.TYPE_CLASS_TEXT  | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

                alertDialog = new AlertDialog.Builder(ShareTrackActivity.this)
                .setView(layout)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Select mail´s source")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String email = mailField.getText().toString();

                        if(validateMail(email)){
                            if(!mails.contains(email)){
                                mails.add(email);
                                adapter.notifyDataSetChanged();
                            }
                        }else{
                            Toast.makeText(ShareTrackActivity.this, "You must enter a valid email", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //dialog.dismiss();
                    }
                })
                .create();

                return alertDialog;

            default: return null;

        }
    }

    private boolean validateMail(String email) {
        String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    //Diálogo que muestra campo de texto para escribir mail y botón aceptar y cancelar
    private void addMailManual(){
        showDialog(DIALOGO_MAIL_MANUAL);
    }

    //Lanzar Activity que permita seleccionar los contactos desde la agenda del teléfono
    private void addMailAgenda(){
        ContactAccessor mContactAccessor = ContactAccessor.getInstance();
        startActivityForResult(mContactAccessor.getPickContactIntent(), PICK_CONTACT_REQUEST);
    }

    /**
     * Invoked when the contact picker activity is finished. The {@code contactUri} parameter
     * will contain a reference to the contact selected by the user. We will treat it as
     * an opaque URI and allow the SDK-specific ContactAccessor to handle the URI accordingly.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {

        switch (requestCode) {

            case PICK_CONTACT_REQUEST:
                //Aquí, en vez de cargar los datos del contacto seleccionado, debería mostrar una pantalla con las
                //direcciones de correo del contacto seleccionado para que el usuario pueda seleccionar alguna
                if(resultCode == RESULT_OK){
                    /*Intent intent = new Intent(Intent.ACTION_PICK, People.CONTENT_URI);
                    startActivityForResult(intent, PICK_EMAIL_REQUEST);*/

                    addMailToList(dataIntent.getData());

                    /*Intent intent = new Intent(ShareTrackActivity.this, ShareTrackActivity.class);
                    intent.setData(dataIntent.getData());
                    startActivityForResult(intent, PICK_EMAIL_REQUEST);*/
                }else{
                    //hacer algo
                    Toast.makeText(ShareTrackActivity.this, "You must select a contact with email filled!", Toast.LENGTH_LONG).show();
                }
                break;

            case PICK_EMAIL_REQUEST:
                if(resultCode == RESULT_OK){
                    //Se ha seleccionado un mail, hay que añadirlo a la lista
                    String email = dataIntent.getStringExtra("email");
                    mails.add(email);
                    adapter.notifyDataSetChanged();
                }else{
                    //hacer algo
                    Toast.makeText(ShareTrackActivity.this, "You must select a contact with email filled!", Toast.LENGTH_LONG).show();
                }
                break;

            default:
                setResult(RESULT_OK, dataIntent);
                finish();
                return;
        }
    }

    private void addMailToList(Uri data){
        List<String> ls = data.getPathSegments();

        String id = ls.get(3);

        // Alternatively, use the Uri method to produce the base URI.
        // It takes a string rather than an integer.

        Cursor cursor = getContentResolver().query( ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                new String[]{id}, null);

        startManagingCursor(cursor);

        cursor.moveToFirst();
        String email = "";

        try {
            email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
        }catch(CursorIndexOutOfBoundsException e){
            Toast.makeText(ShareTrackActivity.this, "You must select a contact with email filled!", Toast.LENGTH_LONG).show();
        }

        if (email != null && !"".equals(email)) {
            if (!mails.contains(email)) {
                mails.add(email);
                adapter.notifyDataSetChanged();
            }
        } else {
            Toast.makeText(ShareTrackActivity.this, "You must select a contact with email filled!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CMD_DELETE, 0, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {

            case CMD_DELETE:
                showFinalAlert("Are you sure yo want to delete this entry?");
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showFinalAlert(CharSequence message) {
        new AlertDialog.Builder(ShareTrackActivity.this).setTitle("Warning")
        .setMessage(message)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                onDeleteItem();
            }
        })
        .setCancelable(false).show();
    }

    private void onDeleteItem() {

        boolean res = mails.remove(itemSelected);

        if (res) {
            adapter.notifyDataSetChanged();
        }
    }

}