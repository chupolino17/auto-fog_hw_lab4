package utils;

import exceptions.GraphException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;


public class DrawingUtils extends JFrame {
    public void drawGraph (String jsonPath, String graphPath) throws IOException, GraphException {
        File consensusResult = new File(jsonPath);
        File graphFile = new File(graphPath);

        BufferedReader lineReader = new BufferedReader(new FileReader(consensusResult));
        String lineText;

        List<Double> probabilities = new ArrayList<>();
        List<Double> expectedMessages = new ArrayList<>();

        Map<Double, Double> coordinates = new TreeMap<>();

        while ((lineText = lineReader.readLine()) != null) {
            for (int i = 0; i < lineText.length(); ++i) {
                if (lineText.charAt(i) == 'p') {
                    if (lineText.startsWith("probability", i)) {
                        i = 13 + i;
                        StringBuilder prob = new StringBuilder();

                        while (lineText.charAt(i) != ',') {
                            prob.append(lineText.charAt(i));
                            ++i;
                        }

                        double probability = Double.parseDouble(prob.toString());

                        if (probability < 0) {
                            throw new GraphException("Probability less than 0");
                        } else if (probability > 1) {
                            throw new GraphException("Probability greater than 1");
                        }

                        probabilities.add(probability);
                    }
                } else if (lineText.charAt(i) == 'e') {
                    if (lineText.startsWith("expectedMessages", i)) {
                        i = 18 + i;
                        StringBuilder expectedMessage = new StringBuilder();

                        while (lineText.charAt(i) != ',') {
                            expectedMessage.append(lineText.charAt(i));
                            ++i;
                        }
                        
                        if (expectedMessage.toString().equals("\"Infinity\"")) {
                            throw new GraphException("Expected number of messages is infinity");
                        }

                        double expMessage = Double.parseDouble(expectedMessage.toString());

                        if (expMessage < 0) {
                            throw new GraphException("Expected number of messages less than 0");
                        }

                        expectedMessages.add(expMessage);
                    }
                }


            }
        }

        for (int i = 0; i < expectedMessages.size(); i++) {
            coordinates.put(expectedMessages.get(i), probabilities.get(i));
        }

        expectedMessages.clear();
        probabilities.clear();

        expectedMessages = new ArrayList<>(coordinates.keySet());
        probabilities = new ArrayList<>(coordinates.values());

        if (probabilities.size() != expectedMessages.size()) {
            throw new GraphException("Size of Probabilities list differs from size of ExpectedMessages list");
        }

        GraphPanel graph = new GraphPanel(expectedMessages, probabilities, graphFile);
        graph.run();
    }

    private static class GraphPanel extends JPanel {
        private final Color lineColor = new Color(57, 174, 214, 180);
        private final Color pointColor = new Color(0, 0, 0, 180);
        private final Color gridColor = new Color(200, 200, 200, 200);
        private final Color highestPointColor = new Color(175, 20, 20, 200);
        private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
        private List<Double> xCoordinates;
        private List<Double> yCoordinates;
        private final File graphFile;

        public GraphPanel(List<Double> xCoordinates, List<Double> yCoordinates, File graphFile) {
            this.xCoordinates = xCoordinates;
            this.yCoordinates = yCoordinates;
            this.graphFile = graphFile;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int padding = 25;
            int labelPadding = 25;

            // draw white background
            graphics.setColor(Color.WHITE);
            graphics.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding, getHeight() - 2 * padding - labelPadding);
            graphics.setColor(Color.BLACK);

            // create hatch marks and grid lines for y-axis.
            int pointWidth = 6;
            int pointHeight = 6;

            int numberYDivisions = 10;
            int numberXDivisions;

            int xStart = (int)getMinScore(xCoordinates);
            double xMax = getMaxScore(xCoordinates);
            double xDiff = xMax - xStart;
            double xIncrementer;

            double yStartDouble = getMinScore(yCoordinates); // done because yStart [0, 1] so extremely small.
            double yMax = getMaxScore(yCoordinates);
            double yDiff = yMax - yStartDouble;
            double yIncrementer = 0.1;

            if (xDiff < 1) {
                double divider = 1.0;
                double xDiffCopy = xDiff;
                while (xDiffCopy < 1) {
                    xDiffCopy *= 10;
                    divider *= 10.0;
                }

                numberXDivisions = (int)xDiffCopy + 1;
                xIncrementer = (numberXDivisions / divider) / numberXDivisions;
            } else {
                numberXDivisions = (int) min(10, ceil(xDiff / 2.0) + 1);
                xIncrementer = round(xDiff / numberXDivisions);
                if (xIncrementer < (xDiff / numberXDivisions)) {
                    ++numberXDivisions;
                }
            }

            if (yDiff <= 0.5) {
                String yStartString = firstDecimal(yStartDouble);
                yStartDouble = Double.parseDouble(yStartString) / 10;
                yDiff = round((yMax - yStartDouble) * 10) / 10.0;

                numberYDivisions = max(0, Integer.parseInt(firstDecimal(yDiff)) - 1);
                yIncrementer = yDiff / (numberYDivisions + 1);
                numberYDivisions += 1;
            } else {
                yStartDouble = 0.0;
            }

            for (int i = 0; i < numberYDivisions + 1; i++) {
                int x0 = padding + labelPadding;
                int x1 = pointWidth + padding + labelPadding;

                int y0 = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);

                if (yCoordinates.size() > 0) {
                    graphics.setColor(gridColor);
                    graphics.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y0);
                    graphics.setColor(Color.BLACK);

                    String yLabel;
                    if (yDiff <= 0.5) {
                        yLabel = (yStartDouble * 10 +  (yIncrementer * i * 10))/10 + "";
                    } else {
                        yLabel = (double)i/10 + "";
                    }


                    if (yLabel.length() > 7) {
                        yLabel = scientificNotation(yLabel);
                    }

                    FontMetrics metrics = graphics.getFontMetrics();
                    int labelWidth = metrics.stringWidth(yLabel);
                    graphics.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);

                    if (numberYDivisions == 1 && i == 1) {
                        String halfWayLabel = (yStartDouble * 10 + yIncrementer * 5) / 10 + "";
                        int y1 = getHeight() - padding - labelPadding;
                        int halfY0 = y1 + (y0 - y1)/ 2;
                        graphics.drawString(halfWayLabel, x0 - labelWidth - 5,  halfY0+ (metrics.getHeight() / 2) - 3);
                        graphics.setColor(gridColor);
                        graphics.drawLine(padding + labelPadding + 1 + pointWidth, halfY0, getWidth() - padding, halfY0);
                    }
                }

                graphics.drawLine(x0, y0, x1, y0);
            }

            // and for x-axis
            for (int i = 0; i < numberXDivisions + 1; i++) {
                if (numberXDivisions > 1) {
                    int x0 = i * (getWidth() - padding * 2 - labelPadding) / (numberXDivisions) + padding + labelPadding;
                    int y0 = getHeight() - padding - labelPadding;
                    int y1 = y0 - pointWidth;

                    if (xCoordinates.size() > 0) {
                        graphics.setColor(gridColor);
                        graphics.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x0, padding);
                        graphics.setColor(Color.BLACK);
                        String xLabel = (xStart * 10 +  (xIncrementer * i * 10))/ 10 + "";
                        FontMetrics metrics = graphics.getFontMetrics();
                        int labelWidth = metrics.stringWidth(xLabel);
                        graphics.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
                    }

                    graphics.drawLine(x0, y0, x0, y1);
                }
            }

            // create x and y axes
            graphics.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
            graphics.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding, getHeight() - padding - labelPadding);

            List<List<Double>> graphPoints = new ArrayList<>();

            for (int i = 0; i < xCoordinates.size(); i++) {
                int xMultiplier = (int) ceil((xCoordinates.get(i) - xStart) / xIncrementer);
                double xScale = (xCoordinates.get(i) - (xMultiplier - 1)*xIncrementer - xStart) / xIncrementer;
                int currentX0 = xMultiplier * (getWidth() - padding * 2 - labelPadding) / (numberXDivisions) + padding + labelPadding;
                double prevX = max(xStart, (double)((xMultiplier - 1) * (getWidth() - padding * 2 - labelPadding) / (numberXDivisions) + padding + labelPadding));
                double x1 = prevX + xScale * ((double) currentX0 - prevX);

                int yMultiplier = (int) ceil((yCoordinates.get(i) - yStartDouble) / yIncrementer);
                int currentY0 = getHeight() - ((yMultiplier * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);
                double yScale = (yCoordinates.get(i) - (yMultiplier - 1)*yIncrementer - yStartDouble) / yIncrementer;
                double prevY = max(yStartDouble, (getHeight() - ((double)((yMultiplier - 1) * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding)));
                double y1 = currentY0 + ((1 - yScale) * (prevY - currentY0));

                graphPoints.add(new ArrayList<>(Arrays.asList(x1, y1)));
            }

            Stroke oldStroke = graphics.getStroke();
            graphics.setColor(lineColor);
            graphics.setStroke(GRAPH_STROKE);

            for (int i = 0; i < graphPoints.size() - 1; i++) {
                Double x1 = graphPoints.get(i).get(0);
                Double y1 = graphPoints.get(i).get(1);

                Double x2 = graphPoints.get(i + 1).get(0);
                Double y2 = graphPoints.get(i + 1).get(1);

                Line2D shape = new Line2D.Double();
                shape.setLine(x1, y1, x2, y2);
                graphics.draw(shape);
            }

            graphics.setStroke(oldStroke);


            for (int i = 0; i < graphPoints.size(); i++) {
                if (yCoordinates.get(i) == yMax) {
                    graphics.setColor(highestPointColor);
                } else {
                    graphics.setColor(pointColor);
                }

                int x = graphPoints.get(i).get(0).intValue() - pointWidth / 2;
                int y = graphPoints.get(i).get(1).intValue() - pointWidth / 2;
                graphics.fillOval(x, y, pointWidth, pointHeight);
            }
        }

        private String trim(String xLabel) {
            int end = xLabel.length();

            for (int i = 3; i < xLabel.length(); i++) {
                if (xLabel.charAt(i) == '0') {
                    end = i;
                    break;
                }
            }

            return xLabel.substring(0, end);
        }

        private double getMinScore(List<Double> lst) {
            double minScore = Double.MAX_VALUE;
            for (Double score : lst) {
                minScore = Math.min(minScore, score);
            }

            return minScore;
        }

        private double getMaxScore(List<Double> lst) {
            double maxScore = Double.MIN_VALUE;
            for (Double score : lst) {
                maxScore = Math.max(maxScore, score);
            }

            return maxScore;
        }

        private String scientificNotation(String str) {
            StringBuilder ans = new StringBuilder("." + str.charAt(2));

            int startIndex = 3;
            while (startIndex < str.length() && str.charAt(startIndex) == '0') {
                ++startIndex;
            }

            ans.append("+").append(str.charAt(startIndex));

            ans.append("E-").append(startIndex - 2);

            return ans.toString();
        }

        private String firstDecimal(Double dob) {
            return Double.toString(dob).substring(2, 3);
        }

        public void setScores(List<Double> xCoordinates, List<Double> yCoordinates) {
            this.xCoordinates = xCoordinates;
            this.yCoordinates = yCoordinates;
            invalidate();
            this.repaint();
        }

        public List<List<Double>> getScores() {
            return new ArrayList<>(Arrays.asList(xCoordinates, yCoordinates));
        }

        public void addTitle(String wantedTitle) {
            TitledBorder title = BorderFactory.createTitledBorder(new EmptyBorder(0, 0, 0, 10), wantedTitle);
            title.setTitleJustification(TitledBorder.CENTER);
            title.setTitleFont(new Font("Arial", Font.BOLD, 20));
            this.setBorder(title);
        }

        private static void createAndShowGui(List<Double> xCoordinates, List<Double> yCoordinates, File imgFile) throws IOException {
            DrawingUtils.GraphPanel.MainPanel mainPanel = new DrawingUtils.GraphPanel.MainPanel(xCoordinates, yCoordinates, "Probability vs expected number of messages");
            mainPanel.setPreferredSize(new Dimension(1000, 800));

            JFrame frame = new JFrame("Probability vs expected number of messages");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(mainPanel);
            frame.pack();

            Container content = frame.getContentPane();
            BufferedImage img = new BufferedImage(frame.getWidth() - 15, frame.getHeight() - 36, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = img.createGraphics();
            content.printAll(g2d);
            g2d.dispose();
            ImageIO.write(img, "png", imgFile);
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        }

        static class MainPanel extends JPanel {
            public MainPanel(List<Double> xCoordinates, List<Double> yCoordinates, String graphTitle) {
                setLayout(new BorderLayout());

                JLabel title = new JLabel(graphTitle);
                title.setFont(new Font("Arial", Font.BOLD, 25));
                title.setHorizontalAlignment(JLabel.CENTER);

                JPanel graphPanel = new DrawingUtils.GraphPanel(xCoordinates, yCoordinates, null);

                DrawingUtils.GraphPanel.VerticalPanel verticalPanel = new DrawingUtils.GraphPanel.VerticalPanel("Probability");

                DrawingUtils.GraphPanel.HorizontalPanel horizontalPanel = new DrawingUtils.GraphPanel.HorizontalPanel("Expected number of messages");

                add(title, BorderLayout.NORTH);
                add(horizontalPanel, BorderLayout.SOUTH);
                add(verticalPanel, BorderLayout.WEST);
                add(graphPanel, BorderLayout.CENTER);
            }
        }

        static class HorizontalPanel extends JPanel {
            final String label;

            public HorizontalPanel(String label) {
                setPreferredSize(new Dimension(0, 25));
                this.label = label;
            }

            @Override
            public void paintComponent(Graphics g) {

                super.paintComponent(g);

                Graphics2D gg = (Graphics2D) g;
                gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Font font = new Font("Arial", Font.BOLD, 15);

                FontMetrics metrics = g.getFontMetrics(font);
                int width = metrics.stringWidth(label);

                gg.setFont(font);

                gg.drawString(label, (getWidth() - width) / 2, 11);
            }
        }

        static class VerticalPanel extends JPanel {
            final String label;

            public VerticalPanel(String label) {
                setPreferredSize(new Dimension(25, 0));
                this.label = label;
            }

            @Override
            public void paintComponent(Graphics g) {

                super.paintComponent(g);

                Graphics2D gg = (Graphics2D) g;
                gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Font font = new Font("Arial", Font.BOLD, 15);

                FontMetrics metrics = g.getFontMetrics(font);
                int width = metrics.stringWidth(label);

                gg.setFont(font);

                drawRotate(gg, getWidth(), (getHeight() + width) / 2.0, 270, label);
            }

            public void drawRotate(Graphics2D gg, double x, double y, int angle, String text) {
                gg.translate((float) x, (float) y);
                gg.rotate(Math.toRadians(angle));
                gg.drawString(text, 0, 0);
                gg.rotate(-Math.toRadians(angle));
                gg.translate(-(float) x, -(float) y);
            }
        }

        public void run() {
            SwingUtilities.invokeLater(() -> {
                try {
                    createAndShowGui(this.xCoordinates, this.yCoordinates, graphFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void main(String[] args) throws IOException, GraphException {
        String result = "result file path";
        String imgDest = "desired file destination path";
        DrawingUtils drawingUtils = new DrawingUtils();
        drawingUtils.drawGraph(result, imgDest);
    }
}
