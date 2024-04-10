import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
                            + " - uploadImage -> Change image of a record \n"
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
                    String nameOfImage = "none";

                    SaveDataRecordRequest request = SaveDataRecordRequest.newBuilder()
                            .setItemId(itemId)
                            .setNameOfProduct(nameOfProduct)
                            .setPrice(price)
                            .setNumberOfItems(numberOfItems)
                            .setNameOfImage(nameOfImage)
                            .build();

                    ReadDataRecordResponse response = bStub.saveDataRecord(request);

                    System.out.println("--> Response: " + response);
                    break;
                }

                case "uploadImage": {
                    System.out.println("Enter the ID of the item to download and update:");
                    int itemId = scanner.nextInt();

                    // Send a request to the server to retrieve the item with the specified ID
                    ReadDataRecordRequest readRequest = ReadDataRecordRequest.newBuilder()
                            .setItemId(itemId)
                            .build();

                    // Receive the item from the server
                    ReadDataRecordResponse itemResponse = bStub.readDataRecord(readRequest);

                    // Check if the item exists
                    if (itemResponse != null) {
                        // Update the nameOfImage for the item
                        System.out.println("Enter the path to the image file to upload:");
                        String imagePath = scanner.next();
                        uploadImage("C:\\Users\\jakub\\IdeaProjects\\gRPC1\\GrpcClient\\src\\image\\" + imagePath, itemResponse.getItemId(), itemResponse, nonBStub);
                        SaveDataRecordRequest newRequest = SaveDataRecordRequest.newBuilder()
                                .setItemId(itemResponse.getItemId())
                                .setNameOfProduct(itemResponse.getNameOfProduct())
                                .setPrice(itemResponse.getPrice())
                                .setNumberOfItems(itemResponse.getNumberOfItems())
                                .setNameOfImage(imagePath)
                                .build();

                    } else {
                        System.out.println("Item not found.");
                    }
                    break;
                }

                case "downloadImage": {
                    System.out.println("Enter the ID of the item to download the image for:");
                    int itemId = scanner.nextInt();

                    ReadDataRecordRequest readRequest = ReadDataRecordRequest.newBuilder()
                            .setItemId(itemId)
                            .build();

                    ReadDataRecordResponse itemResponse = bStub.readDataRecord(readRequest);

                    String filename = null;
                    if (itemResponse != null) {
                        if (itemResponse.getItemId() == itemId) {
                            filename = itemResponse.getNameOfImage();
                            break;
                        }
                    }

                    if (filename == null) {
                        System.out.println("Item with ID " + itemId + " not found.");
                        break;
                    }

                    // Create the request
                    DownloadImageRequest request = DownloadImageRequest.newBuilder()
                            .setFilename(filename)
                            .build();

                    // Call the server's downloadImage method
                    String finalFilename = filename;
                    nonBStub.downloadImage(request, new StreamObserver<DownloadImageResponse>() {
                        @Override
                        public void onNext(DownloadImageResponse response) {
                            // Save the received image data to a file
                            try (FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\jakub\\IdeaProjects\\gRPC1\\GrpcClient\\src\\image\\" + finalFilename)) {
                                response.getData().writeTo(fileOutputStream);
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.err.println("Error saving image file: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            System.err.println("Error downloading image: " + throwable.getMessage());
                        }

                        @Override
                        public void onCompleted() {
                            System.out.println("Image downloaded successfully!");
                        }
                    });
                    break;
                }

                case "getAll": {
                    System.out.println("Requesting all records from the server...");
                    // Requesting all records from the server
                    Iterator<ReadDataRecordResponse> recordsIterable = bStub.readAllDataRecords(MyEmpty.newBuilder().build());

                    ArrayList<ReadDataRecordResponse> records = new ArrayList<>();
                    recordsIterable.forEachRemaining(records::add);

                    // Printing out all records
                    System.out.println("All records received from the server:");
                    for (ReadDataRecordResponse record : records) {
                        System.out.println(record);
                    }
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

    private static String uploadImage(String imagePath, int itemId, ReadDataRecordResponse itemResponse, DataManagementGrpc.DataManagementStub nonBStub) {
        try (FileInputStream fileInputStream = new FileInputStream(imagePath)) {
            StreamObserver<UploadImageRequest> requestObserver = nonBStub.uploadImage(new StreamObserver<UploadImageResponse>() {
                @Override
                public void onNext(UploadImageResponse response) {
                }

                @Override
                public void onError(Throwable throwable) {
                    System.err.println("Error uploading image: " + throwable.getMessage());
                }

                @Override
                public void onCompleted() {
                    System.out.println("\nImage uploaded successfully!");
                }
            });

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                System.out.print("*");
                requestObserver.onNext(UploadImageRequest.newBuilder()
                        .setData(ByteString.copyFrom(buffer, 0, bytesRead))
                        .setId(itemId)
                        .build());
            }

            requestObserver.onCompleted();
            return null;
        } catch (IOException e) {
            System.err.println("Error reading image file: " + e.getMessage());
        }
        return null;
    }

}
