import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class GrpcServer {

    static ArrayList<Record> listOfRecords;

    public static void main(String[] args) {
        int port = 50001;
        listOfRecords = new ArrayList<>();
        System.out.println("Starting gRPC server...");
        Server server = ServerBuilder.forPort(port)
                .addService(new DataManagementImpl())
                .build();
        try {
            server.start();
            System.out.println("...Server started");
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class DataManagementImpl extends DataManagementGrpc.DataManagementImplBase {
        @Override
        public void saveDataRecord(SaveDataRecordRequest request, StreamObserver<ReadDataRecordResponse> responseObserver) {
            System.out.println("Received saveDataRecord request");
            Record newRecord = Record.newBuilder()
                    .setItemId(request.getItemId())
                    .setNameOfProduct(request.getNameOfProduct())
                    .setPrice(request.getPrice())
                    .setNumberOfItems(request.getNumberOfItems())
                    .setNameOfImage(request.getNameOfImage())
                    .build();

            listOfRecords.add(newRecord);

            ReadDataRecordResponse response = ReadDataRecordResponse.newBuilder()
                    .setItemId(newRecord.getItemId())
                    .setNameOfProduct(newRecord.getNameOfProduct())
                    .setPrice(newRecord.getPrice())
                    .setNumberOfItems(newRecord.getNumberOfItems())
                    .setNameOfImage(newRecord.getNameOfImage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void readDataRecord(ReadDataRecordRequest request, StreamObserver<ReadDataRecordResponse> responseObserver) {
            System.out.println("Received readDataRecord request");
            int itemId = request.getItemId();
            for (Record record : listOfRecords) {
                if (record.getItemId() == itemId) {
                    responseObserver.onNext(ReadDataRecordResponse.newBuilder()
                            .setItemId(record.getItemId())
                            .setNameOfProduct(record.getNameOfProduct())
                            .setPrice(record.getPrice())
                            .setNumberOfItems(record.getNumberOfItems())
                            .setNameOfImage(record.getNameOfImage())
                            .build());
                    responseObserver.onCompleted();
                    return;
                }
            }
            responseObserver.onError(new RuntimeException("Record not found"));
        }

        @Override
        public void readAllDataRecords(Empty request, StreamObserver<ReadDataRecordResponse> responseObserver) {
            System.out.println("Received readAllDataRecords request");
            for (Record record : listOfRecords) {
                responseObserver.onNext(ReadDataRecordResponse.newBuilder()
                        .setItemId(record.getItemId())
                        .setNameOfProduct(record.getNameOfProduct())
                        .setPrice(record.getPrice())
                        .setNumberOfItems(record.getNumberOfItems())
                        .setNameOfImage(record.getNameOfImage())
                        .build());
            }
            responseObserver.onCompleted();
        }

        @Override
        public void searchRecordsByValue(SearchRecordsByValueRequest request, StreamObserver<SearchRecordsByValueResponse> responseObserver) {
            System.out.println("Received searchRecordsByValue request");
            int value = request.getValue();
            SearchRecordsByValueResponse.Builder responseBuilder = SearchRecordsByValueResponse.newBuilder();
            for (Record record : listOfRecords) {
                if (record.getPrice() == value) {
                    responseBuilder.addRecords(ReadDataRecordResponse.newBuilder()
                            .setItemId(record.getItemId())
                            .setNameOfProduct(record.getNameOfProduct())
                            .setPrice(record.getPrice())
                            .setNumberOfItems(record.getNumberOfItems())
                            .setNameOfImage(record.getNameOfImage())
                            .build());
                }
            }
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        }

        @Override
        public StreamObserver<UploadImageRequest> uploadImage(StreamObserver<UploadImageResponse> responseObserver) {
            System.out.println("Received uploadImage request");
            return new StreamObserver<>() {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                @Override
                public void onNext(UploadImageRequest request) {
                    try {
                        outputStream.write(request.getData().toByteArray());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    String filename = UUID.randomUUID().toString() + ".jpg"; // Generating unique filename
                    try (FileOutputStream fileOutputStream = new FileOutputStream("server_images/" + filename)) {
                        outputStream.writeTo(fileOutputStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    UploadImageResponse response = UploadImageResponse.newBuilder()
                            .setFilename(filename)
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }
            };
        }

        @Override
        public void downloadImage(DownloadImageRequest request, StreamObserver<DownloadImageResponse> responseObserver) {
            System.out.println("Received downloadImage request");
            String filename = request.getFilename();
            try (FileInputStream fileInputStream = new FileInputStream("server_images/" + filename)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    responseObserver.onNext(DownloadImageResponse.newBuilder().setData(
                            ByteString.copyFrom(buffer, 0, bytesRead)).build());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            responseObserver.onCompleted();
        }
    }
}
