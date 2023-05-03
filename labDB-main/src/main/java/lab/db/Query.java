package lab.db;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lab.model.Cliente;
import lab.model.PostazioneOmbrellone;
import lab.model.TipoCliente;
import lab.utils.Utils;

public class Query {

	private final Connection connection;

	public Query(Connection connection) {
		this.connection = connection;
	}
	
	public List<Integer> getStagioni() throws SQLException {
		var stagioni = new ArrayList<Integer>();
		ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM Spiagge ORDER BY anno DESC");
		while (rs.next()) {
			stagioni.add(rs.getInt("anno"));
        }
		return stagioni;
	}
	
	public void addStagione(int stagione) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("INSERT INTO Spiagge VALUES (?)");
		statement.setInt(1, stagione);
		statement.executeUpdate();
	}
	
	public List<PostazioneOmbrellone> getOmbrelloniPiantati(int anno) throws SQLException {
		String query = "SELECT * FROM PostazioniOmbrelloni WHERE anno = ?";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setInt(1, anno);
		return PostazioneOmbrellone.readPostazioniOmbrelloniFromResultSet(statement.executeQuery());
	}
	
	public boolean isOmbrellonePiantato(int numeroOmbrellone, int anno, Date dataInizio, Date dataFine) throws SQLException {
		String query = "SELECT * FROM PostazioniOmbrelloni WHERE anno = ? AND numeroOmbrellone = ? AND dataInizio <= ? AND (dataFine >= ? OR dataFine is null)";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setInt(1, anno);
		statement.setInt(2, numeroOmbrellone);
		statement.setDate(3, Utils.dateToSqlDate(dataInizio));
		statement.setDate(4, Utils.dateToSqlDate(dataFine));
		return statement.executeQuery().next();
	}
	
	public boolean isOmbrellonePrenotato(int numeroOmbrellone, int anno, Date dataInizio, Date dataFine) throws SQLException {
		String query = "SELECT * FROM OmbrelloniConPrenotazione WHERE anno = ? AND numeroOmbrellone = ? AND NOT ((dataInizio < ? AND dataFine < ?) OR (dataInizio > ? AND dataFine > ?))";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setInt(1, anno);
		statement.setInt(2, numeroOmbrellone);
		statement.setDate(3, Utils.dateToSqlDate(dataInizio));
		statement.setDate(4, Utils.dateToSqlDate(dataInizio));
		statement.setDate(5, Utils.dateToSqlDate(dataFine));
		statement.setDate(6, Utils.dateToSqlDate(dataFine));
		return statement.executeQuery().next();
	}
	
	public List<Integer> getNumeriOmbrelloni(int anno) throws SQLException {
		var numeriOmbrelloni = new ArrayList<Integer>();
		String query = "SELECT numeroOmbrellone FROM PostazioniOmbrelloni WHERE anno = ?";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setInt(1, anno);
		var resultSet = statement.executeQuery();
		while (resultSet.next()) {
			numeriOmbrelloni.add(resultSet.getInt("numeroOmbrellone"));
        }
		return numeriOmbrelloni;
	}
	
	public void insertPostazioneOmbrellone(int numeroOmbrellone, int fila, int colonna, int anno, Date dataInizio) throws SQLException {
		String query = "INSERT INTO PostazioniOmbrelloni (anno, numeroOmbrellone, dataInizio, fila, colonna) SELECT ?, ?, ?, ?, ? WHERE 100 >= (SELECT COUNT(*) FROM PostazioniOmbrelloni WHERE anno = ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, anno);
        statement.setInt(2, numeroOmbrellone);
        statement.setDate(3, Utils.dateToSqlDate(dataInizio));
        statement.setInt(4, fila);
        statement.setInt(5, colonna);
        statement.setInt(6, anno);
        statement.executeUpdate();
	}
	
	public List<Integer> getNumeriSedute(int anno) throws SQLException {
		var numeriSedute = new ArrayList<Integer>();
		String query = "SELECT numeroSeduta FROM PostazioniSeduteRiva WHERE anno = ?";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setInt(1, anno);
		var resultSet = statement.executeQuery();
		while (resultSet.next()) {
			numeriSedute.add(resultSet.getInt("numeroSeduta"));
        }
		return numeriSedute;
	}
	
	public void insertPostazioneSeduta(int numeroSeduta, int anno) throws SQLException {
		String query = "INSERT INTO PostazioniSeduteRiva VALUES (?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, anno);
		statement.setInt(2, numeroSeduta);
        statement.executeUpdate();
	}
	
	public boolean removePostazioneOmbrellone(int numeroOmbrellone, int anno, Date dataFine) throws SQLException {
		String query = "UPDATE PostazioniOmbrelloni SET dataFine = ? WHERE anno = ? AND numeroOmbrellone = ? "
				+ "AND ((NOT EXISTS (SELECT 1 FROM OmbrelloniConPrenotazione WHERE anno = ? AND numeroOmbrellone = ?)) "
				+ "OR (? >= (SELECT MAX(dataFine) FROM OmbrelloniConPrenotazione WHERE anno = ? AND numeroOmbrellone = ?)))";
		
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setDate(1, Utils.dateToSqlDate(dataFine));
		statement.setInt(2, anno);
		statement.setInt(3, numeroOmbrellone);
		statement.setInt(4, anno);
		statement.setInt(5, numeroOmbrellone);
		statement.setDate(6, Utils.dateToSqlDate(dataFine));
		statement.setInt(7, anno);
		statement.setInt(8, numeroOmbrellone);
		return statement.executeUpdate() == 0 ? false : true;
	}
	
	public List<TipoCliente> getTipiClienti() throws SQLException {
		String query = "SELECT * FROM TipiClienti";
		ResultSet rs = connection.createStatement().executeQuery(query);
		var tipiClienti = new ArrayList<TipoCliente>();
		while (rs.next()) {
			tipiClienti.add(new TipoCliente(rs.getInt("codiceTipoCliente"),  rs.getString("nome")));
        }
		return tipiClienti;
	}
	
	public List<Cliente> getClienti() throws SQLException {
		String query = "SELECT codiceFiscale, C.nome, cognome, telefono, T.nome AS tipoCliente FROM Clienti C LEFT JOIN TipiClienti T ON C.codiceTipoCliente = T.codiceTipoCliente;";
		ResultSet rs = connection.createStatement().executeQuery(query);
		var clienti = new ArrayList<Cliente>();
		while (rs.next()) {
			clienti.add(new Cliente(rs.getString("codiceFiscale"),  rs.getString("nome"), rs.getString("cognome"), rs.getString("telefono"), rs.getString("tipoCliente")));
        }
		return clienti;
	}
}
