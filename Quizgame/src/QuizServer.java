import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class QuizServer {

    private static Map<String, String> quizQuestions = new HashMap<>();
    private static AtomicInteger clientCount = new AtomicInteger(0); // 연결된 클라이언트 수 추적
    
    static {
        quizQuestions.put("What is the capital of France?", "Paris");
        quizQuestions.put("What is 5 + 7?", "12");
        quizQuestions.put("What is the color of the sky?", "Blue");
    }

    public static String checkAnswer(String question, String answer) {
        String correctAnswer = quizQuestions.get(question);
        return correctAnswer != null && correctAnswer.equalsIgnoreCase(answer) ? "Correct!" : "Incorrect. The correct answer is " + correctAnswer;
    }

    public static void main(String[] args) {
        System.out.println("The server is running...");
        ExecutorService pool = Executors.newFixedThreadPool(20); // 20개의 스레드 풀 생성

        try (ServerSocket serverSocket = new ServerSocket(7777)) { // 포트 7777에서 서버 시작
            while (true) {
                Socket clientSocket = serverSocket.accept();
                int clientId = clientCount.incrementAndGet(); // 새로운 클라이언트 ID 부여
                
                // 클라이언트의 IP 주소, 원격 포트, 서버의 로컬 포트 정보 출력
                System.out.println("Client #" + clientId + " connected from " 
                                   + clientSocket.getInetAddress().getHostAddress() 
                                   + ":" + clientSocket.getPort() 
                                   + " to local port " + clientSocket.getLocalPort());
                
                pool.execute(new QuizGame(clientSocket, clientId)); // 각 클라이언트를 스레드 풀에서 처리
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            pool.shutdown(); // 서버 종료 시 스레드 풀 종료
        }
    }

    private static class QuizGame implements Runnable {
        private Socket socket;
        private int score = 0;
        private int clientId; // 클라이언트 ID 저장

        public QuizGame(Socket socket, int clientId) {
            this.socket = socket;
            this.clientId = clientId; // 클라이언트 ID 설정
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

                for (String question : quizQuestions.keySet()) {
                    out.write("Question: " + question + "\n");
                    out.flush();
                    String clientAnswer = in.readLine();
                    if (clientAnswer == null || clientAnswer.equalsIgnoreCase("bye")) {
                        System.out.println("Client #" + clientId + " has disconnected.");
                        break;
                    }

                    String result = checkAnswer(question, clientAnswer);
                    out.write(result + "\n");
                    out.flush();
                    
                    if (result.equals("Correct!")) {
                        score++;
                    }
                }
                out.write("Quiz finished! Your total score is: " + score + "\n");
                out.flush();
                System.out.println("Client #" + clientId + " quiz finished with score: " + score); // 최종 점수 출력
            } catch (IOException e) {
                System.out.println("Error with Client #" + clientId + ": " + e.getMessage());
            } finally {
                try {
                    socket.close();
                    System.out.println("Connection with Client #" + clientId + " closed.");
                } catch (IOException e) {
                    System.out.println("Error closing connection with Client #" + clientId);
                }
            }
        }
    }
}
