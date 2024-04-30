package com.example;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class CurrencyConverterApp extends JFrame {

    private JTextField amountField;
    private JComboBox<String> fromCurrencyComboBox;
    private JComboBox<String> toCurrencyComboBox;
    private JButton convertButton;
    private JLabel resultLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }

            CurrencyConverterApp converterApp = new CurrencyConverterApp();
            converterApp.setVisible(true);
        });
    }

    public CurrencyConverterApp() {
        setTitle("Currency Converter");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeUI();
        loadCurrencyList();
    }

    private void initializeUI() {
        amountField = new JTextField(10);
        fromCurrencyComboBox = new JComboBox<>();
        toCurrencyComboBox = new JComboBox<>();
        convertButton = new JButton("Convert");
        resultLabel = new JLabel("Result: ");

        
        setLayout(new FlowLayout());

        add(new JLabel("Amount: "));
        add(amountField);
        add(new JLabel("From Currency: "));
        add(fromCurrencyComboBox);
        add(new JLabel("To Currency: "));
        add(toCurrencyComboBox);
        add(convertButton);
        add(resultLabel);

       
        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertCurrency();
            }
        });
    }

    private void loadCurrencyList() {
        try {
            URL url = new URL("https://api.coinbase.com/v2/currencies");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            connection.disconnect();

           
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray currenciesArray = jsonResponse.getJSONArray("data");

            for (int i = 0; i < currenciesArray.length(); i++) {
                JSONObject currencyObject = currenciesArray.getJSONObject(i);
                String currencyId = currencyObject.getString("id");
                fromCurrencyComboBox.addItem(currencyId);
                toCurrencyComboBox.addItem(currencyId);
            }

        } catch (IOException | JSONException exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading currency list. Check your internet connection.");
        }
    }

    private void convertCurrency() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            String fromCurrency = (String) fromCurrencyComboBox.getSelectedItem();
            String toCurrency = (String) toCurrencyComboBox.getSelectedItem();

            if (fromCurrency.equals(toCurrency)) {
                resultLabel.setText("Result: Please select different currencies.");
                return;
            }

            
            URL apiUrl = new URL("https://api.coinbase.com/v2/exchange-rates?currency=" + fromCurrency);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            connection.disconnect();

            
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONObject ratesObject = jsonResponse.getJSONObject("data").getJSONObject("rates");
            double exchangeRate = ratesObject.getDouble(toCurrency);

            
            double convertedAmount = amount * exchangeRate;

            DecimalFormat df = new DecimalFormat("#.##");
            resultLabel.setText("Result: " + df.format(convertedAmount) + " " + toCurrency);

        } catch (NumberFormatException | IOException | JSONException exception) {
            exception.printStackTrace();
            resultLabel.setText("Result: Invalid input or error in conversion.");
        }
    }
}

