import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Process implements ProcessInterface{

    public String[] neighbors;
    public boolean isInitiator;
    public int received = 0;
    public String id;
    public List<String> alreadySended;
    public static String textDecipher;
    public int cipherReceived = 0;

    public Process(String[] neighbors, boolean isInitiator, String id) throws RemoteException  {
        this.neighbors = neighbors;
        this.isInitiator = isInitiator;
        this.id = id;
    }

    public static void main(String[] args) throws IOException {
        //input
        String id = args[0];
        String[] neighbors = args[1].split(",");
        boolean isInitiator = Boolean.parseBoolean(args[2]);
        String route = args[3];
        String ip = args[4];
        if(isInitiator) {
            String route = args[3];
            String ip = args[4];
        }

        try {
            Process obj = new Process(neighbors, isInitiator, id);
            ProcessInterface stub2 = (ProcessInterface) UnicastRemoteObject.exportObject(obj, 0);
            // Bind the remote object's stub in the registry
            try {
                LocateRegistry.createRegistry(2000);
            } catch (RemoteException e) { }
            Registry registry = LocateRegistry.getRegistry(2000);
            try {
                registry.bind(id, stub2);
            } catch (AlreadyBoundException e){
                System.out.println("Node already bound to registry \n");
            }
            System.err.println("Node " + id + " is  ready");
            if(isInitiator){
                stub2.send(id);
                if (System.getSecurityManager() == null) {
                    System.setSecurityManager(new SecurityManager());
                }
                String remoteHostName = ip;
                String name = "/PublicKey";
                String connectLocation = "//" + remoteHostName + name;
                InterfaceServer hello = null;
                String textCipher = null;
                try {
                    System.out.println("Connecting to client at : " + connectLocation);
                    hello = (InterfaceServer) Naming.lookup(connectLocation);
                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                } catch (NotBoundException e1) {
                    e1.printStackTrace();
                }

                String key = null;
                try {
                    key = hello.getKey("grupo_1");
                    System.out.println("La llave es:"+key);
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    textCipher = new String(Files.readAllBytes(Paths.get(route)));
                    System.out.println("El texto cifrado es:" + textCipher);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                textDecipher = null;
                try {
                    textDecipher = hello.decipher("grupo_1", textCipher, key);
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("Texto decifrado es :" + textDecipher);

                stub2.message(id, textDecipher);
            }
        } catch (RemoteException e) {
            System.out.println("Couldnt bind node to registry\n");
            e.printStackTrace();
        }

    }

    @Override
    public String send (String idOrigin) throws RemoteException {
        if (isInitiator) {
            if(!id.equals(idOrigin))
                return "Repre";
            Registry reg = LocateRegistry.getRegistry(2000);
            alreadySended = new ArrayList<>();
            for(String ids : neighbors){
                if(!alreadySended.contains(ids)) {
                    try {
                        ProcessInterface stub = (ProcessInterface) reg.lookup(ids);
                        System.out.println("Sending explorer to " + ids);
                        alreadySended.add(ids);
                        stub.send(id);
                    } catch (NotBoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            while(received < neighbors.length){
                continue;
            }
            System.out.println(idOrigin + ": I'm ready with responses");
            return null;
        }
        else {
            System.out.println("Explorer received from process " + idOrigin);
            String parent = idOrigin;
            received += 1;
            Registry reg = LocateRegistry.getRegistry(2000);
            ProcessInterface stub;
            alreadySended = new ArrayList<>();
            for(String ids : neighbors){
                if(!ids.equals(parent) && !alreadySended.contains(ids)){
                    try{
                        stub = (ProcessInterface) reg.lookup(ids);
                        alreadySended.add(ids);
                        System.out.println("Sending explorer to " + ids);
                        String value = stub.send(id);
                        if(value != null){
                            received += 1;
                            System.out.println("The node " + ids + " is the coordinator");
                        }
                    } catch (NotBoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            while(received < neighbors.length){
                continue;
            }
            try {
                stub = (ProcessInterface) reg.lookup(parent);
                System.out.println("Sending OK message to " + idOrigin);
                stub.sendOk(id, parent);
            } catch (NotBoundException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    public String sendOk (String from, String to) throws RemoteException{
        if(!id.equals(to)) {
            try {
                Registry reg = LocateRegistry.getRegistry(2000);
                ProcessInterface stub = (ProcessInterface) reg.lookup(to);
                System.out.println("Sending OK to :" + to);
                stub.sendOk(from, to);
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println(from + " Replied with Ok...");
            received += 1;
        }
        return null;
    }

    @Override
    public String message (String idOrigin, String messages) throws RemoteException {
        if (isInitiator && cipherReceived == 0) {
            cipherReceived = 1;
            Registry reg = LocateRegistry.getRegistry(2000);
            alreadySended = new ArrayList<>();
            for(String ids : neighbors){
                if(!alreadySended.contains(ids)) {
                    try {
                        ProcessInterface stub = (ProcessInterface) reg.lookup(ids);
                        System.out.println("Sending message to " + ids);
                        alreadySended.add(ids);
                        stub.message(id, textDecipher);
                    } catch (NotBoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
        else {
            if (cipherReceived != 1) {
                System.out.println("Message received from " + idOrigin);
                System.out.println("The message is " + messages);
                String parent = idOrigin;
                cipherReceived = 1;
                Registry reg = LocateRegistry.getRegistry(2000);
                ProcessInterface stub;
                alreadySended = new ArrayList<>();
                for (String ids : neighbors) {
                    if (!ids.equals(parent) && !alreadySended.contains(ids)) {
                        try {
                            stub = (ProcessInterface) reg.lookup(ids);
                            alreadySended.add(ids);
                            System.out.println("Sending this message to " + ids);
                            String value = stub.message(id, messages);
                        } catch (NotBoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        }
    }

}


