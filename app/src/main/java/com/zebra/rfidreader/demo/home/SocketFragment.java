package com.zebra.rfidreader.demo.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfidreader.demo.BuildConfig;
import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.application.Application;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.common.ResponseHandlerInterfaces;
import com.zebra.rfidreader.demo.inventory.InventoryListItem;
import com.zebra.rfidreader.demo.rfid.RFIDController;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SocketFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SocketFragment extends Fragment implements View.OnClickListener {

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
    private PrintWriter  output;
    boolean is_connect_success;


    boolean enable_run_thread_client;
    boolean enable_run_thread_server;
    boolean enable_run_thread_receive_data;
    boolean enable_run_thread_send_data;


    ServerSocket serverSocket;
    Thread Thread_Server = null;
    List<Socket> list_clients;

    CountDownTimer timeout_socket;



    class Active_Socket_Server implements Runnable {
        int number_client = 0;
        boolean isException = false;
        String str_exception = null;
        @Override
        public void run() {
            try {
                if(serverSocket != null)
                    return;
                Log.e(TAG, "zzzzzzzz");
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(socket_port));
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        socket_status.setText("Opened Server. Waiting client ...");
                    }
                });
                try {
                    Socket socket = serverSocket.accept();
                    list_clients.add(socket);
                    timeout_socket.cancel();
                    output = new PrintWriter(socket.getOutputStream(), true);
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    enable_run_thread_receive_data = true;
                    enable_run_thread_send_data = true;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            socket_status.setText("Client connected");
                        }
                    });
                    new Thread(new Receiver_Data(number_client, Constants.SOCKET_MODE_SERVER)).start();
                    number_client++;


                } catch (IOException e) {
                    isException = true;
                    str_exception = e.getMessage();

                    Log.e(TAG, "Active_Socket_Server, socket server accept: "+e.getMessage());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            socket_status.setText(e.getMessage());
                        }
                    });
                }
            } catch (IOException e) {
                isException = true;
                str_exception = e.getMessage();

            }finally {
                if(isException)
                {
                    Exception_Case(str_exception, Constants.Exception_Connect, 0);
                    str_exception = null;
                    isException = false;
                }
                else
                {
                    is_connect_success = serverSocket.isBound();

                    if(is_connect_success)
                    {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buttonConnect.setEnabled(true);
                                buttonConnect.setText("Openned");
                            }
                        });
                    }
                }
            }
        }
    }




    void Exception_Case(String str_exception, int exception_mode, int index_socket)
    {
        if(socket_station_mode == Constants.SOCKET_MODE_CLIENT)
        {
            Log.e(TAG, "thread Active_Socket_Client exception: " + str_exception);
            if(exception_mode != Constants.Exception_EXIT)
            {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        socket_status.setText(str_exception);
                    }
                });
            }


            enable_run_thread_client = false;
            enable_run_thread_receive_data = false;
            enable_run_thread_send_data = false;
            if(output != null) {
                output.close();
                output = null;
            }
            if(input != null){
                try {
                    input.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                input = null;
            }

            if(socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                is_connect_success = false;
                if(exception_mode != Constants.Exception_EXIT)
                {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (exception_mode)
                            {
                                case Constants.Exception_Server_Close: socket_status.setText("Lost Connect"); break;
                                case Constants.Exception_Close: socket_status.setText("Closed"); break;
                            }

                            buttonConnect.setEnabled(true);
                            buttonConnect.setText("Connect");
                        }
                    });
                }
                socket = null;
            }

            if(Thread_Client != null)
            {
                Thread_Client.interrupt();
                Thread_Client = null;
            }
        }
        else if (socket_station_mode == Constants.SOCKET_MODE_SERVER)
        {
            Log.e(TAG, "thread Active_Socket_Server exception: " + str_exception + ", index = " + index_socket);
            if(exception_mode != Constants.Exception_EXIT)
            {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        socket_status.setText(str_exception + "(index: " + index_socket + ")");
                    }
                });
            }



            enable_run_thread_receive_data = false;
            enable_run_thread_send_data = false;
            if(output != null) {
                output.close();
                output = null;
            }
            if(input != null){
                try {
                    input.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                input = null;
            }
            if(!list_clients.isEmpty())
            {
                if(list_clients.get(index_socket) != null) {
                    try {
                        list_clients.get(index_socket).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    is_connect_success = false;
                    if(exception_mode != Constants.Exception_EXIT)
                    {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buttonConnect.setEnabled(true);
                                buttonConnect.setText("Open");
                            }
                        });
                    }

                    list_clients.remove(index_socket);
                    Log.e(TAG, "thread Active_Socket_Server exception: list client = " + list_clients.size());

                }
            }

            /*if(exception_mode == Constants.Exception_Close)*/
            {
                enable_run_thread_server = false;
                if(serverSocket != null)
                {
                    try {
                        serverSocket.setReuseAddress(true);
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "serverSocket.close() = " + e.getMessage());
                    }
                    serverSocket = null;
                }

                if(Thread_Server != null)
                {
                    Thread_Server.interrupt();
                    Thread_Server = null;
                }
            }
            /*else if(exception_mode == Constants.Exception_Client_Disconnect)
            {

            }*/

        }


    }
    class Active_Socket_Client implements Runnable {
        int number_client = 0;
        boolean isException = false;
        String str_exception = null;
        public void run() {
            try {
                Log.e(TAG, "thread Active_Socket_Client: " + 1);
                if(socket == null) {
                    socket = new Socket();
                    SocketAddress socket_address = new InetSocketAddress(socket_ip, socket_port);
                    int timeout = 2000;
                    socket.connect(socket_address, timeout);
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    output = new PrintWriter(socket.getOutputStream(), true);
                    enable_run_thread_receive_data = true;
                    enable_run_thread_send_data = true;
                    new Thread(new Receiver_Data(number_client, Constants.SOCKET_MODE_CLIENT)).start();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "thread Active_Socket_Client: Connected ok");
                            socket_status.setText("Connected");
                        }
                    });
                    number_client++;
                }
            } catch (IOException e) {
                isException = true;
                str_exception = e.getMessage();
            } finally {
                if(isException)
                {
                    Exception_Case(str_exception, Constants.Exception_Connect, 0);
                    str_exception = null;
                    isException = false;
                }
                else
                {
                    is_connect_success = socket.isConnected();
                    if(is_connect_success)
                    {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                socket_status.setText("Connected");
                                buttonConnect.setEnabled(true);
                                buttonConnect.setText("Connected");
                            }
                        });
                    }
                }
            }
        }
    }

    class Receiver_Data implements Runnable {
        int index = 0;
        int mode_station = Constants.SOCKET_MODE_DONT_USE;
        String str_heading = null;
        Receiver_Data(int index, int mode_station)
        {
            this.index = index;
            this.mode_station = mode_station;
            switch (this.mode_station)
            {
                case Constants.SOCKET_MODE_DONT_USE:
                    str_heading = String.format("Thread Receiver Data [%d], mode Don't use", index);
                    break;
                case Constants.SOCKET_MODE_CLIENT:
                    str_heading = String.format("Thread Receiver Data [%d], mode Client", index);
                    break;
                case Constants.SOCKET_MODE_SERVER:
                    str_heading = String.format("Thread Receiver Data [%d], mode Server", index);
                    break;
            }
        }
        @Override
        public void run() {
            if(mode_station == Constants.SOCKET_MODE_DONT_USE)
            {
                Log.i(TAG, str_heading +  " -> do nothing");
                return;
            }

            Log.i(TAG, str_heading + " begin ----------------------------" + index);
            while (enable_run_thread_receive_data && input != null) {
                try {
                    final String data = input.readLine();

                    Log.i(TAG, str_heading + " message receiver: " + data);
                    if (data != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addListViewData(data);
                            }
                        });
                    } else {
                        if(mode_station == Constants.SOCKET_MODE_CLIENT)
                        {
                            //care Server close connect
                            Exception_Case("Server closed", Constants.Exception_Server_Close, 0);
                        }
                        else if(mode_station == Constants.SOCKET_MODE_SERVER)
                        {
                            //care Client disconnect
                            Exception_Case("Client disconnect", Constants.Exception_Client_Disconnect, index);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, str_heading + " error: "+e.getMessage());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            socket_status.setText(e.getMessage());
                        }
                    });
                }
            }
            Log.i(TAG, str_heading + " end ----------------------------"+ index);
        }
    }
    class Send_Message implements Runnable {
        private String message;

        int mode_station = Constants.SOCKET_MODE_DONT_USE;
        String str_heading = null;

        Send_Message(String message, int mode_station) {
            this.message = message;
            this.mode_station = mode_station;

            if(this.mode_station == Constants.SOCKET_MODE_CLIENT)
                this.str_heading = "Thread Send Message, mode Client";
            else  if (this.mode_station == Constants.SOCKET_MODE_SERVER)
                this.str_heading = "Thread Send Message, mode Server";
        }
        @Override
        public void run() {
            Log.i(TAG, this.str_heading + " begin ----------------------------");
            if(!enable_run_thread_send_data && output == null)
                return;

            output.write(message);
            output.flush();
            Log.i(TAG, this.str_heading + "sent message: " + message);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    socket_status.setText("Sent " + message);
                }
            });

            Log.i(TAG, this.str_heading + " end ----------------------------");
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

        Thread_Server = null;
        serverSocket = null;
        Thread_Client = null;
        socket = null;
        input = null;
        output = null;
        is_connect_success = false;

        list_clients = new ArrayList<Socket>();
        enable_run_thread_server = false;
        enable_run_thread_client = false;
        enable_run_thread_receive_data = false;
        enable_run_thread_send_data = false;



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

        //countdown
        timeout_socket = new CountDownTimer(10000,10000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                    Exception_Case("Close socket server",Constants.Exception_Close, 0);
                    buttonConnect.setEnabled(true);
            }
        };
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
        //toask_message("added " + data);
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
            new Thread(new Send_Message(message, socket_station_mode)).start();
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConnectServer:
                switch (socket_station_mode)
                {
                    case Constants.SOCKET_MODE_DONT_USE:
                        break;
                    case Constants.SOCKET_MODE_CLIENT:
                        if(!is_connect_success)
                        {
                            enable_run_thread_client = true;
                            if(Thread_Client == null)
                            {
                                Thread_Client = new Thread(new Active_Socket_Client());
                                Thread_Client.start();
                                buttonConnect.setEnabled(false);
                            }

                        }
                        else {

                            is_connect_success = false;
                            Exception_Case("Close socket",Constants.Exception_Close, 0);
                        }
                        break;
                    case Constants.SOCKET_MODE_SERVER:
                        if(!is_connect_success)
                        {
                            enable_run_thread_server = true;
                            if(Thread_Server == null)
                            {
                                Thread_Server = new Thread(new Active_Socket_Server());
                                Thread_Server.start();
                                buttonConnect.setEnabled(false);
                                timeout_socket.start();
                            }

                        }
                        else {

                            is_connect_success = false;
                            Exception_Case("Close socket server",Constants.Exception_Close, 0);
                        }
                        break;
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

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        timeout_socket.cancel();
        switch (socket_station_mode)
        {
            case Constants.SOCKET_MODE_CLIENT:
                if(is_connect_success){
                    Exception_Case("Close socket",Constants.Exception_EXIT, 0);
                    is_connect_success = false;
                }
                break;
            case Constants.SOCKET_MODE_SERVER:
                if(is_connect_success){

                    is_connect_success = false;
                    Exception_Case("Close socket server",Constants.Exception_EXIT, 0);
                }
                break;
        }
        Log.i(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");

    }
}