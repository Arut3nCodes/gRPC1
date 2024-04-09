import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Scanner;

public class GrpcClient {
    public static void main(String[] args) {
        String address = "localhost";
        int port = 50001;
        boolean isRunning = true;
        DataManagementGrpc.DataManagementBlockingStub bStub;
        DataManagementGrpc.DataManagementStub nonBStub;
        System.out.println("Running gRPC client...");
        Scanner scanner = new Scanner(System.in);

        // Building the channel for communication
        ManagedChannel channel = ManagedChannelBuilder.forAddress(address, port)
                .usePlaintext().build();

        // Building the blocking stub
        bStub = DataManagementGrpc.newBlockingStub(channel);
        nonBStub = DataManagementGrpc.newStub(channel);


        while(isRunning){
            String message = scanner.next();
            switch(message){
                case "help":{
                    System.out.println("List of all commands: \n"
                            + " - add -> Add new record \n"
                            + " - changeImageOfRecord [idOfRecord] -> Change image of a record \n"
                            + " - getAll -> List all records on the server \n"
                            + " - exit -> Finish program \n");
                    break;
                }
                case "exit":{
                    isRunning = false;
                    System.out.println("--Shutdown--");
                    break;
                }
                case "add": {
                    System.out.println("Enter item id:");
                    int itemId = scanner.nextInt();
                    System.out.println("Enter name of product:");
                    String nameOfProduct = scanner.next();
                    System.out.println("Enter price:");
                    double price = scanner.nextDouble();
                    System.out.println("Enter number of items:");
                    int numberOfItems = scanner.nextInt();
                    System.out.println("Enter name of image:");
                    String nameOfImage = scanner.next();

                    // Building the request object
                    SaveDataRecordRequest request = SaveDataRecordRequest.newBuilder()
                            .setItemId(itemId)
                            .setNameOfProduct(nameOfProduct)
                            .setPrice(price)
                            .setNumberOfItems(numberOfItems)
                            .setNameOfImage(nameOfImage)
                            .build();

                    // Calling the remote procedure synchronously
                    ReadDataRecordResponse response = bStub.saveDataRecord(request);

                    System.out.println("--> Response: " + response);
                    break;
                }
                case "changeImageOfRecord": {
                    // Logic to change image of a record
                    break;
                }
                case "getAll": {
                    // Logic to get all records from the server
                    break;
                }
                default:{
                    System.out.println("--Command not found - type 'help' to list all commands--");
                    break;
                }
            }
        }
        channel.shutdown();
    }

    private static class UnaryObs implements StreamObserver<Record> {
        public void onNext(Record record) {
            System.out.println("-->async unary onNext: " + record);
        }

        public void onError(Throwable throwable) {
            System.out.println("-->async unary onError");
        }

        public void onCompleted() {
            System.out.println("-->async unary onCompleted");
        }
    }
}
