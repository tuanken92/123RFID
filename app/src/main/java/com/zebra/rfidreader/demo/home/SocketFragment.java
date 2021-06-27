package com.zebra.rfidreader.demo.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.rfidreader.demo.BuildConfig;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.rfid.RFIDController;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SocketFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SocketFragment extends Fragment implements View.OnClickListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "atk";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    Button buttonClear;
    Button buttonConnect;
    Button buttonSend;
    EditText ip_server;
    EditText data_to_send;
    ListView list_data_receive;
    TextView socket_status;


    int socket_station_mode;
    String socket_ip;
    int socket_port;

    List<String> list_mode; //show UI

    List<String> list_string_data_receive;
    ArrayAdapter<String> arrayAdapter;  //for display listview



    //Replace below IP with the IP of that device in which server socket open.
    //If you change port then change the port number in the server side code also.
    Thread Thread_Client = null;
    Socket socket;
    private BufferedReader input;
    private BufferedWriter output;
    boolean is_connect_success;
    boolean is_enable_run_client;

    class Active_Socket_Client implements Runnable {
        public void run() {
            try {
                socket = new Socket(socket_ip, socket_port);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        socket_status.setText("Connected\n");
                    }
                });
                new Thread(new Receiver_Data()).start();
            } catch (IOException e) {
                Log.e(TAG, "thread Active_Socket_Client: " + e.getMessage());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        socket_status.setText(e.getMessage());
                    }
                });
            }
        }
    }
    class Receiver_Data implements Runnable {
        @Override
        public void run() {
            while (is_enable_run_client) {
                try {
                    final String message = input.readLine();
                    Log.i(TAG, "message from server: " + message);
                    if (message != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addListViewData(message);
                            }
                        });
                    } else {
                        Thread_Client = new Thread(new Active_Socket_Client());
                        Thread_Client.start();
                        return;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "thread Receiver_Data: "+e.getMessage());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            socket_status.setText(e.getMessage());
                        }
                    });
                }
            }
        }
    }
    class Send_Message implements Runnable {
        private String message;
        Send_Message(String message) {
            this.message = message;
        }
        @Override
        public void run() {
            if(output == null)
                return;
            try {
                output.write(message);
                output.flush();
                Log.i(TAG, "sent message to server: " + message);
            } catch (IOException e) {
                Log.e(TAG, "thread Send_Message" + e.getMessage());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        socket_status.setText(e.getMessage());
                    }
                });

            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    socket_status.setText("Sent " + message);
                }
            });
        }
    }


    private void initializeConnectionSettings() {
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);

        //socket
        //true = server, false = client
        socket_station_mode = settings.getInt(Constants.SOCKET_STATION_TYPE, Constants.SOCKET_MODE_DONT_USE);
        socket_ip = settings.getString(Constants.SOCKET_STATION_IP, "127.0.0.1");
        socket_port = settings.getInt(Constants.SOCKET_STATION_PORT, 8888);
        //end socket

        list_mode = new ArrayList<String>();
        list_mode.add("Socket don't use");
        list_mode.add("Socket run as mode Server");
        list_mode.add("Socket run as mode Client");

    }

    public void Init_UI()
    {
        //init var
        is_connect_success = false;
        list_string_data_receive = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list_string_data_receive);


        //init gui
        buttonClear = (Button) getActivity().findViewById(R.id.btnClearData);
        buttonConnect = (Button) getActivity().findViewById(R.id.btnConnectServer);
        buttonSend = (Button) getActivity().findViewById(R.id.btnSendData);

        buttonClear.setOnClickListener(this);
        buttonConnect.setOnClickListener(this);
        buttonSend.setOnClickListener(this);


        ip_server = (EditText) getActivity().findViewById(R.id.editTextIPServer);
        data_to_send = (EditText) getActivity().findViewById(R.id.editTextDataToSend);

        list_data_receive = (ListView) getActivity().findViewById(R.id.list_receive_data);
        list_data_receive.setAdapter(arrayAdapter);

        socket_status = (TextView) getActivity().findViewById(R.id.socket_status);

        ip_server.setText(socket_ip + ":" + socket_port);
        socket_status.setText(list_mode.get(socket_station_mode));
    }

    public SocketFragment() {
        // Required empty public constructor
    }

    public static SocketFragment newInstance() {
        return new SocketFragment();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SocketFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SocketFragment newInstance(String param1, String param2) {
        SocketFragment fragment = new SocketFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        initializeConnectionSettings();
        Log.i(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_socket, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated");
        Init_UI();
    }

    public void toask_message(String message)
    {
        Toast.makeText(this.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    void addListViewData(String data)
    {
        String currentDateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        list_string_data_receive.add(0, currentDateTime + ": " + data);
        arrayAdapter.notifyDataSetChanged();
        toask_message("added " + data);
    }

    void clearListViewData()
    {
        list_string_data_receive.clear();
        arrayAdapter.notifyDataSetChanged();
        toask_message("Clear data done!");
    }

    void hideKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
    }

    void Send_Message()
    {
        String message = data_to_send.getText().toString().trim();
        if (!message.isEmpty()) {
            new Thread(new Send_Message(message)).start();
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConnectServer:
                if(!is_connect_success)
                {
                    //connect
                    is_enable_run_client = true;
                    Thread_Client = new Thread(new Active_Socket_Client());
                    Thread_Client.start();
                }
                else {

                    is_connect_success = false;
                    Thread_Client.interrupt();
                }

                break;
            case R.id.btnSendData:
                hideKeyboard();
                Send_Message();
                break;
            case R.id.btnClearData:
                clearListViewData();
                break;
            default:
                break;
        }
    }
}