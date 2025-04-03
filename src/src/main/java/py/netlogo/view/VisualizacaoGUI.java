package src.main.java.py.netlogo.view;

import py.netlogo.controller.SimulacaoController;
import py.netlogo.model.Agente;
import py.netlogo.model.Recurso;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class VisualizacaoGUI extends JFrame {
    private final SimulacaoController controller;
    private final JPanel simulationPanel;
    private final JLabel turnLabel;
    private final JLabel statusLabel;
    private final Map<String, ImageIcon> imageCache;
    private Timer simulationTimer;

    public VisualizacaoGUI(SimulacaoController controller) {
        this.controller = controller;
        this.imageCache = new HashMap<>();

        setTitle("NetLogo Paraguai - Simulação de Agentes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLayout(new BorderLayout());

        // Painel de controle
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);

        // Painel de simulação
        simulationPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                renderSimulation(g);
            }
        };
        simulationPanel.setPreferredSize(new Dimension(800, 800));
        add(new JScrollPane(simulationPanel), BorderLayout.CENTER);

        // Painel de status
        JPanel statusPanel = new JPanel();
        turnLabel = new JLabel("Turno: 0");
        statusLabel = new JLabel("Pronto para iniciar");
        statusPanel.add(turnLabel);
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);

        // Configura o timer para atualização automática
        setupSimulationTimer();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();

        JButton startButton = new JButton("Iniciar Simulação");
        startButton.addActionListener(e -> {
            controller.iniciarSimulacao("paraguai_1");
            updateView();
        });

        JButton nextTurnButton = new JButton("Próximo Turno");
        nextTurnButton.addActionListener(e -> {
            controller.executarTurno();
            updateView();
        });

        JButton autoButton = new JButton("Execução Automática");
        autoButton.addActionListener(e -> toggleAutoSimulation());

        panel.add(startButton);
        panel.add(nextTurnButton);
        panel.add(autoButton);

        return panel;
    }

    private void setupSimulationTimer() {
        simulationTimer = new Timer(500, (ActionEvent e) -> {
            if (controller.isSimulacaoAtiva()) {
                controller.executarTurno();
                updateView();
            } else {
                simulationTimer.stop();
            }
        });
    }

    private void toggleAutoSimulation() {
        if (simulationTimer.isRunning()) {
            simulationTimer.stop();
            statusLabel.setText("Simulação pausada");
        } else {
            simulationTimer.start();
            statusLabel.setText("Simulação em execução automática");
        }
    }

    private void updateView() {
        if (controller.getSimulacao() != null) {
            turnLabel.setText("Turno: " + controller.getSimulacao().getTurnoAtual() +
                    "/" + controller.getSimulacao().getMaxTurnos());

            if (!controller.isSimulacaoAtiva()) {
                statusLabel.setText("Simulação concluída");
            }
        }
        simulationPanel.repaint();
    }

    private void renderSimulation(Graphics g) {
        if (controller.getSimulacao() == null) return;

        Ambiente ambiente = controller.getSimulacao().getAmbiente();
        int cellSize = calculateCellSize(ambiente);

        // Renderizar fundo
        renderBackground(g, ambiente, cellSize);

        // Renderizar recursos
        renderResources(g, ambiente, cellSize);

        // Renderizar agentes
        renderAgents(g, controller.getSimulacao(), cellSize);
    }

    private int calculateCellSize(Ambiente ambiente) {
        int maxWidth = simulationPanel.getWidth() / ambiente.getTamanho().width;
        int maxHeight = simulationPanel.getHeight() / ambiente.getTamanho().height;
        return Math.min(maxWidth, maxHeight);
    }

    private void renderBackground(Graphics g, Ambiente ambiente, int cellSize) {
        g.setColor(new Color(200, 230, 200)); // Verde claro para o Paraguai
        g.fillRect(0, 0,
                ambiente.getTamanho().width * cellSize,
                ambiente.getTamanho().height * cellSize);

        // Grade
        g.setColor(Color.GRAY);
        for (int x = 0; x <= ambiente.getTamanho().width; x++) {
            g.drawLine(x * cellSize, 0, x * cellSize, ambiente.getTamanho().height * cellSize);
        }
        for (int y = 0; y <= ambiente.getTamanho().height; y++) {
            g.drawLine(0, y * cellSize, ambiente.getTamanho().width * cellSize, y * cellSize);
        }
    }

    private void renderResources(Graphics g, Ambiente ambiente, int cellSize) {
        for (Recurso recurso : ambiente.getRecursos()) {
            if (!recurso.isColetado()) {
                ImageIcon icon = getImageIcon(recurso.getImagemPath());
                g.drawImage(icon.getImage(),
                        recurso.getPosicao().x * cellSize,
                        recurso.getPosicao().y * cellSize,
                        cellSize, cellSize, this);
            }
        }
    }

    private void renderAgents(Graphics g, Simulacao simulacao, int cellSize) {
        for (Agente agente : simulacao.getAgentes()) {
            ImageIcon icon = getImageIcon(agente.getImagemPath());
            g.drawImage(icon.getImage(),
                    agente.getPosicao().x * cellSize,
                    agente.getPosicao().y * cellSize,
                    cellSize, cellSize, this);

            // Info do agente
            g.setColor(Color.BLACK);
            g.drawString(agente.getNome() + ": " + agente.getRecursosColetados(),
                    agente.getPosicao().x * cellSize,
                    agente.getPosicao().y * cellSize - 5);
        }
    }

    private ImageIcon getImageIcon(String path) {
        if (!imageCache.containsKey(path)) {
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource(path));
                imageCache.put(path, icon);
            } catch (Exception e) {
                // Fallback para ícone padrão
                ImageIcon defaultIcon = createDefaultIcon();
                imageCache.put(path, defaultIcon);
            }
        }
        return imageCache.get(path);
    }

    private ImageIcon createDefaultIcon() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillOval(0, 0, 32, 32);
        g2d.dispose();
        return new ImageIcon(img);
    }
}