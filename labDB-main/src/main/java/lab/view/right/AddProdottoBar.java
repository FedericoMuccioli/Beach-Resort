package lab.view.right;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.SQLException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import lab.db.Query;
import lab.model.FasciaOraria;
import lab.model.Prodotto;
import lab.model.TipoProdotto;
import lab.utils.Utils;

public class AddProdottoBar extends JDialog {

	public AddProdottoBar(final Query query) throws SQLException {
		final var panel = new JPanel(new GridBagLayout());
		panel.setPreferredSize(new Dimension(800,300));
		setTitle("Aggiungi Prodotto Bar");

		var nomeLabel = new JLabel("Nome:");
		var nome = new JTextField("nome", 16);
		var descrizioneLabel = new JLabel("Descrizione:");
		var descrizione = new JTextField("descrizione", 16);
		var tipoLabel = new JLabel("Tipo:");
		var tipoProdotto = new JComboBox<TipoProdotto>();
		var fasciaOrariaLabel = new JLabel("Fascia oraria:");
		var fasciaOrariaModel = new DefaultListModel<FasciaOraria>();
		var fasciaOraria = new JList<FasciaOraria>(fasciaOrariaModel);
		var fasciaOrariaPanel = new JScrollPane(fasciaOraria);
		var prezzoLabel = new JLabel("Prezzo:");
		var prezzo = new JTextField("000.00", 16);
		var alert = new JLabel();
		var button = new JButton("AGGIUNGI");
		
		tipoProdotto.setPreferredSize(prezzo.getPreferredSize());
		Dimension dim = prezzo.getPreferredSize();
		dim.height *= 2;
		fasciaOrariaPanel.setPreferredSize(dim);
		fasciaOrariaPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		tipoProdotto.setModel(new DefaultComboBoxModel<TipoProdotto>(query.getTipiProdotti().toArray(new TipoProdotto[0])));
		fasciaOrariaModel.addAll(query.getFasceOrarie());
		fasciaOrariaModel.add(0, new FasciaOraria(null, null));
		fasciaOraria.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		fasciaOraria.setSelectedIndex(0);


		button.addActionListener(l -> {
			try {
				Prodotto prodotto = new Prodotto(nome.getText(), descrizione.getText(), (TipoProdotto) tipoProdotto.getSelectedItem(), null, Double.parseDouble(prezzo.getText()));
				query.insertProdotto(prodotto);
				
				if (!fasciaOraria.isSelectedIndex(0)) {
					query.insertDisponibilità(prodotto, fasciaOraria.getSelectedValuesList());
				}
				
				alert.setText("Inserimento eseguito");
				Utils.closeJDialogAfterOneSecond(this);
			} catch (final Exception e) {
				alert.setText("Inserimento non eseguito");
			}
		});

		var c = new GridBagConstraints();
		c.gridy = 0;
		panel.add(nomeLabel, c);
		panel.add(nome, c);
		c.gridy++;
		panel.add(descrizioneLabel, c);
		panel.add(descrizione, c);
		c.gridy++;
		panel.add(tipoLabel, c);
		panel.add(tipoProdotto, c);
		c.gridy++;
		panel.add(fasciaOrariaLabel, c);
		panel.add(fasciaOrariaPanel, c);
		c.gridy++;
		panel.add(prezzoLabel, c);
		panel.add(prezzo, c);
		c.gridy++;
		c.gridwidth = 2;
		panel.add(button, c);
		c.gridy++;
		panel.add(alert, c);
		add(panel);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
