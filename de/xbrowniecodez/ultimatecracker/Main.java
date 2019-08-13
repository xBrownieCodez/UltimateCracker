package de.xbrowniecodez.ultimatecracker;

import java.awt.event.*;

import javax.swing.*;

import de.xbrowniecodez.ultimatecracker.api.RoundedBorder;
import de.xbrowniecodez.ultimatecracker.methods.DirectLeaksMethod;
import de.xbrowniecodez.ultimatecracker.methods.SpigotMethod;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

@SuppressWarnings("serial")
public class Main extends JFrame {
	public static String version = "1.0.1";
	private JTextField field;

	public static void main(String[] args) {
		createGUI();
	}

	private static void createGUI() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				} catch (Exception ex) {
					
				}
				Main InfiniteCracker = new Main();
				InfiniteCracker.getContentPane().setBackground(Color.BLACK);
				InfiniteCracker.setTitle("UltimateCracker " + version + " @xBrownieCodez");
				InfiniteCracker.setResizable(false);
				InfiniteCracker.setSize(700, 300);
				InfiniteCracker.setLocationRelativeTo(null);
				InfiniteCracker.setDefaultCloseOperation(3);
				InfiniteCracker.getContentPane().setLayout(null);
				JButton startButton = new JButton("");
				startButton.setIcon(
						new ImageIcon(Main.class.getResource("/de/xbrowniecodez/ultimatecracker/stuff/StrtBtn.png")));
				startButton.setForeground(Color.BLACK);
				startButton.setBackground(Color.WHITE);
				startButton.setBounds(480, 208, 200, 50);
				startButton.setBorder(new RoundedBorder(40));
				startButton.setOpaque(false);
				startButton.setFocusPainted(false);
				InfiniteCracker.getContentPane().add(startButton);
				JTextField pathField = new JTextField();
				pathField.setEnabled(false);
				pathField.setEditable(false);
				pathField.setBounds(0, 0, 6, 20);
				InfiniteCracker.getContentPane().add(pathField);
				pathField.setVisible(false);
				InfiniteCracker.field = pathField;
				JLabel image = new JLabel("");
				image.setIcon(new ImageIcon(Main.class.getResource("/de/xbrowniecodez/ultimatecracker/stuff/logo.png")));
				image.setFont(new Font("Trebuchet MS", Font.BOLD, 31));
				image.setForeground(Color.WHITE);
				image.setBounds(-26, 11, 680, 172);
				InfiniteCracker.getContentPane().add(image);
				image.setBackground(Color.DARK_GRAY);
				image.setLabelFor(InfiniteCracker);
				JButton selectButton = new JButton("");
				selectButton.setIcon(
						new ImageIcon(Main.class.getResource("/de/xbrowniecodez/ultimatecracker/stuff/SlctBtn.png")));
				selectButton.setForeground(Color.BLACK);
				selectButton.setBounds(10, 208, 200, 50);
				InfiniteCracker.getContentPane().add(selectButton);
				selectButton.setBackground(Color.WHITE);
				selectButton.setToolTipText("Select jar file");
				selectButton.setBorder(new RoundedBorder(40));
				selectButton.setOpaque(false);
				selectButton.setFocusPainted(false);
				InfiniteCracker.setIconImage(Toolkit.getDefaultToolkit()
						.getImage(getClass().getResource("/de/xbrowniecodez/ultimatecracker/stuff/icon.png")));
				JComboBox methodSelector = new JComboBox();
				((JLabel) methodSelector.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
				methodSelector.setForeground(Color.WHITE);
				methodSelector.setBackground(Color.BLACK);
				methodSelector.setBounds(220, 170, 250, 20);
				methodSelector.addItem("Select Method");
				methodSelector.addItem("Spigot AntiPiracy Remover");
				methodSelector.addItem("DirectLeaks AntiReleak Remover");
				InfiniteCracker.getContentPane().add(methodSelector);
				JLabel updateLabel = new JLabel("Checking  for updates...");
				updateLabel.setFont(new Font("Trebuchet MS", Font.BOLD, 15));
				updateLabel.setForeground(Color.YELLOW);
				updateLabel.setBounds(254, 139, 205, 20);
				InfiniteCracker.getContentPane().add(updateLabel);
				try {
					URLConnection urlConnection = new URL("https://api.ultimateleaks.com/backend/infinitecracker/version")
							.openConnection();
					urlConnection.setRequestProperty("User-Agent", "UltimateTool");
					urlConnection.setConnectTimeout(100);
					urlConnection.connect();
					BufferedReader bufferedReader = new BufferedReader(
							new InputStreamReader(urlConnection.getInputStream(), Charset.forName("UTF-8")));
					String line;
					while ((line = bufferedReader.readLine()) != null) {
						if (line.equals(Main.version)) {
							updateLabel.setText("Latest version.");
							updateLabel.setForeground(Color.GREEN);
							updateLabel.setBounds(286, 138, 205, 20);
						} else {
							updateLabel.setText("Update available.");
							updateLabel.setForeground(Color.YELLOW);
							updateLabel.setBounds(286, 138, 205, 20);
						}
					}
					bufferedReader.close();
				} catch (IOException var1_2) {
					updateLabel.setText("Error while checking for updates.");
					updateLabel.setForeground(Color.RED);
					updateLabel.setBounds(225, 138, 255, 20);
					
				}

				selectButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JFileChooser chooser = new JFileChooser();

						if (InfiniteCracker.field.getText() != null && !InfiniteCracker.field.getText().isEmpty()) {
							chooser.setSelectedFile(new File(InfiniteCracker.field.getText()));
						}
						chooser.setMultiSelectionEnabled(false);
						chooser.setFileSelectionMode(0);
						int result = chooser.showOpenDialog(InfiniteCracker);
						startButton.setEnabled(true);
						if (result == 0) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									InfiniteCracker.field.setText(chooser.getSelectedFile().getAbsolutePath());
								}
							});
						}
					}
				});
				startButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (InfiniteCracker.field.getText() == null || InfiniteCracker.field.getText().isEmpty()
								|| !InfiniteCracker.field.getText().endsWith(".jar")) {
							JOptionPane.showMessageDialog(null, "You must select a valid jar file!", "Error", 0);
							return;
						}
						File output = null;
						String selectedMethod = (String) methodSelector.getSelectedItem();
						if (selectedMethod.equals("Select Method")) {
							JOptionPane.showMessageDialog(null, "Select a method first!", "Error", 1);

						} else if (selectedMethod.equals("Spigot AntiPiracy Remover")) {
							try {
								File input = new File(InfiniteCracker.field.getText());
								if (!input.getName().endsWith(".jar")) {
									throw new IllegalArgumentException("File must be a jar.");
								}
								if (!input.exists()) {
									throw new FileNotFoundException("The file " + input.getName() + " doesn't exist.");
								}
								output = new File(String.format("%s-Cracked.jar", input.getAbsolutePath().substring(0,
										input.getAbsolutePath().lastIndexOf("."))));
								if (output.exists()) {
									output.delete();
								}
								SpigotMethod.process(input, output, 0);
								if (SpigotMethod.userID == null) {
									JOptionPane.showMessageDialog(null, "Could not find Spigot Anti-Piracy method.",
											"Done", 1);
								} else {
									SpigotMethod.process(input, output, 1);
									SpigotMethod.checkFile(output);
									JOptionPane.showMessageDialog(null, "Done: " + output.getAbsolutePath(), "Done", 1);

								}

							} catch (Throwable t) {
								JOptionPane.showMessageDialog(null, t, "Error", 0);
								t.printStackTrace();
								if (output != null) {
									output.delete();
								}
							} finally {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										InfiniteCracker.field.setText("");
									}
								});
								SpigotMethod.userID = null;
							}
						} else if (selectedMethod.equals("DirectLeaks AntiReleak Remover")) {
							try {
								File input = new File(InfiniteCracker.field.getText());
								if (!input.getName().endsWith(".jar")) {
									throw new IllegalArgumentException("File must be a jar.");
								}
								if (!input.exists()) {
									throw new FileNotFoundException("The file " + input.getName() + " doesn't exist.");
								}
								output = new File(String.format("%s-Output.jar", input.getAbsolutePath().substring(0,
										input.getAbsolutePath().lastIndexOf("."))));
								if (output.exists()) {
									output.delete();
								}
								DirectLeaksMethod.process(input, output);
								DirectLeaksMethod.checkFile(output);
								JOptionPane.showMessageDialog(null, "Done: " + output.getAbsolutePath(), "Done", 1);
							} catch (Throwable t) {
								JOptionPane.showMessageDialog(null, t, "Error", 0);
								t.printStackTrace();
								if (output != null) {
									output.delete();
								}
							} finally {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										InfiniteCracker.field.setText("");
									}
								});
							}
						}
					}
				});
				InfiniteCracker.setVisible(true);
			}
		});
	}

}