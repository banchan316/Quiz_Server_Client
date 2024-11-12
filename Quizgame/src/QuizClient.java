import java.io.*;
import java.net.*;
import java.util.*;

public class QuizClient {
    public static void main(String[] args) {
        BufferedReader in = null;
        BufferedWriter out = null;
        Socket socket = null;
        Scanner scanner = new Scanner(System.in);

        // 서버 정보 파일 읽기
        String serverAddress = "localhost"; // 기본 값
        int port = 7777; // 기본 값

        try (BufferedReader fileReader = new BufferedReader(new FileReader("server_info.dat"))) {
            // 파일에서 읽기 시도
            serverAddress = fileReader.readLine().split("=")[1].trim();
            port = Integer.parseInt(fileReader.readLine().split("=")[1].trim());
        } catch (IOException | ArrayIndexOutOfBoundsException | NumberFormatException e) {
            // 파일을 읽지 못하거나 잘못된 형식일 경우 예외 처리
            System.out.println("Could not read server_info.dat. Using default values.");
            System.out.println("Error: " + e.getMessage()); // 예외 메시지 출력
        }

        try {
            socket = new Socket(serverAddress, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            System.out.println("Connected to the quiz server.");

            while (true) {
                String question = in.readLine();
                if (question == null || question.contains("Quiz finished")) {
                    System.out.println(question);
                    break;
                }
                
                System.out.println(question);
                System.out.print("Your answer: ");
                String answer = scanner.nextLine();
                
                if (answer.equalsIgnoreCase("bye")) {
                    out.write(answer + "\n");
                    out.flush();
                    break;
                }

                out.write(answer + "\n");
                out.flush();

                String feedback = in.readLine();
                System.out.println(feedback);
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                scanner.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.out.println("Error closing the connection.");
            }
        }
    }
}
