package it.unical.sadstudents.mediaplayeruid.model;

import it.unical.sadstudents.mediaplayeruid.Settings;
import it.unical.sadstudents.mediaplayeruid.utils.MyNotification;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager {
    //VARIABLE
    private Connection connection;
    private String url = "jdbc:sqlite:MediaPlayerDb.db";
    //END VARIABLE

    //SINGLETION
    private static DatabaseManager instance = null;

    private DatabaseManager(){connect();}

    public static DatabaseManager getInstance(){
        if (instance==null)
            instance = new DatabaseManager();
        return instance;
    }
    //END SINGLETION

    //GET AND SETTER
    public String getUrl() {return url;}
    //END GET AND SETTER

    //FUNCTION
    //  function private
    private boolean checkLibrary(String name){
        if(name.equals("MusicLibrary") || name.equals("VideoLibrary") ||name.equals("empty") ) return true;
        return  false;
    }
    private boolean isPresent(String object,String key, String tab){
        try {
            if (connection != null && !connection.isClosed()) {
                String query = "select * from "+tab+" where "+object+"=?;";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1,key);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {return  true;}
                stmt.close();
            }
        }catch (SQLException e){}
        return  false;
    }
    private boolean isPresentInt(String object,Integer key, String tab){
        try {
            if (connection != null && !connection.isClosed()) {
                String query = "select * from "+tab+" where "+object+"=?;";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1,key);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {return  true;}
                stmt.close();
            }
        }catch (SQLException e){}
        return  false;
    }
    private boolean checkTable(String table){
        if(table.equals("MyMedia") ||table.equals("Playlist") ||table.equals("Settings") ||
           table.equals("RecentMedia") ||table.equals("Playqueue") ||table.equals("MyMediaPlaylist") ||table.equals("Equalizer"))
            return true;
        else  return false;
    }
    //  end function private

    public boolean connect(){
        try {
            connection = DriverManager.getConnection(url);
            if(connection != null && !connection.isClosed()) {
                return true;
            }
        }catch (SQLException e){
            MyNotification.notifyError("Error","Database connection error",3);
        }
        return false;
    }
    public boolean disconnect(){
        try {
            if(connection!=null && !connection.isClosed()) {
                connection.close();
                return  true;
            }
        }catch (SQLException e){
            MyNotification.notifyError("Error","Database disconnect error",3);
        }
        return  false;
    }


    public boolean addMedia(MyMedia myMedia){
        try {
            if(connection != null && myMedia!=null && !connection.isClosed()&&!isPresent("Path",myMedia.getPath(),"MyMedia")) {
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO MyMedia VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
                stmt.setString(1, myMedia.getPath());
                stmt.setString(2, myMedia.getTitle());
                stmt.setString(3, myMedia.getArtist());
                stmt.setString(4, myMedia.getGenre());
                stmt.setString(5, myMedia.getAlbum());
                stmt.setString(6, myMedia.getImageUrl());
                stmt.setString(7, myMedia.getLength());
                stmt.setString(8, myMedia.getYear());
                stmt.setInt(9,0);
                stmt.setInt(10,0);
                stmt.execute();
                stmt.close();

                return true;
            }
        }catch (SQLException e) {}
        return false;
    }
    public boolean createPlaylist(String name,String image,Integer songs,String duration){
        try {
            if(connection != null && name!=null && !connection.isClosed()&&!isPresent("Name",name,"PlayList")) {
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO Playlist VALUES(?,?,?,?);");
                stmt.setString(1, name);
                stmt.setString(2,image);
                stmt.setInt(3,songs);
                stmt.setString(4,duration);
                stmt.execute();
                return true;
            }
        }catch (SQLException e){}
        return false;
    }
    public boolean addMyMediaInPlaylist(String pathMedia,String name,int Key){
        try {
            if(connection != null && pathMedia!=null && name!=null && !connection.isClosed()) {
                while(isPresentInt("Key",Key,"MyMediaPlaylist"))
                    Key++;
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO MyMediaPlaylist VALUES(?, ?, ?);");
                stmt.setInt(1,Key);
                stmt.setString(2, pathMedia);
                stmt.setString(3,   name);
                stmt.execute();
                stmt.close();
                return true;
            }
        }catch (SQLException e){}
        return false;
    }
    public boolean insertRecentMedia(MyMedia myMedia){
        try {
            if(connection != null&&myMedia!=null&&!connection.isClosed()) {
                if (!isPresent("Path",myMedia.getPath(),"MyMedia")) {
                    addMedia(myMedia);
                }
                if(!isPresent("Path",myMedia.getPath(),"RecentMedia")) {
                    PreparedStatement stmt = connection.prepareStatement("INSERT INTO RecentMedia (Path) VALUES(?)");
                    stmt.setString(1, myMedia.getPath());
                    stmt.execute();
                    stmt.close(); //???
                    return  true;
                }
            }
        }catch (SQLException e){}
        return false;
    }
    public boolean insertPlayQueue(String path,int Key){
        try {
            if(connection != null && path!=null && Key!=-1 && !connection.isClosed()){
                while(isPresentInt("Key",Key,"Playqueue"))
                    Key++;
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO Playqueue VALUES(?,?);");
                stmt.setInt(1,Key);
                stmt.setString(2, path);
                stmt.execute();
                stmt.close();
                return true;
            }
        }catch (SQLException e){}
        return false;
    }


    public boolean setLibrary(String pathMedia,String nameLibrary){
        try {
            if(connection != null && pathMedia!=null &&checkLibrary(nameLibrary) &&!connection.isClosed()&&isPresent("Path",pathMedia,"MyMedia")) {
                PreparedStatement stmt = connection.prepareStatement("UPDATE MyMedia SET MusicLibrary=?, VideoLibrary=? WHERE Path=?;");
                if(nameLibrary.equals("MusicLibrary")){
                    stmt.setInt(1,1);
                    stmt.setInt(2,0);
                }else if(nameLibrary.equals("VideoLibrary")){
                    stmt.setInt(1,0);
                    stmt.setInt(2,1);
                }else{
                    stmt.setInt(1,0);
                    stmt.setInt(2,0);
                }
                stmt.setString(3,pathMedia);
                stmt.execute();
                stmt.close();
                return true;
            }
        }catch (SQLException e) {}
        return false;
    }
    public boolean setMediaString(String date,String tipe,String pathMedia){
        try {
            if(connection != null && date!=null &&tipe!=null&&!connection.isClosed()) {
                PreparedStatement stmt = connection.prepareStatement("UPDATE MyMedia SET "+tipe+ "=? WHERE path=?;");
                stmt.setString(1,date);
                stmt.setString(2,pathMedia);
                stmt.execute();
                stmt.close();
                return true;
            }
        }catch (SQLException e) {}
        return false;

    }
    public boolean setPlaylistSong(Integer songs,String duration,String name){
        try {
            if(connection != null && songs!=-1 &&duration!=null&&!connection.isClosed()) {
                PreparedStatement stmt = connection.prepareStatement("UPDATE Playlist SET Songs=?,TotalDuration=? WHERE Name=?;");
                stmt.setInt(1,songs);
                stmt.setString(2,duration);
                stmt.setString(3,name);
                stmt.execute();
                stmt.close();
                return true;
            }
        }catch (SQLException e) {}
        return false;
    }

    public boolean changePlaylist(String NewName,String OldName,String Image){
        try {

            if(connection != null && NewName!=null&&OldName!=null &&Image!=null&& !connection.isClosed()) {
                PreparedStatement stmt = connection.prepareStatement
                        ("UPDATE Playlist " +
                        " SET Name=?, Image=?" +
                                " WHERE Name=?");
                stmt.setString(1, NewName);
                stmt.setString(2,Image);
                stmt.setString(3,OldName);
                stmt.execute();
                stmt.close();
                return changMediaPlaylist(NewName,OldName);
            }
        }catch (SQLException e){}
        return false;
    }
    private boolean changMediaPlaylist(String NewName,String OldName){
        try {
            if(connection != null && !connection.isClosed()) {
                PreparedStatement stmt = connection.prepareStatement
                        ("UPDATE MyMediaPlaylist " +
                                " SET Name=?" +
                                " WHERE Name=?");
                stmt.setString(1, NewName);
                stmt.setString(2,OldName);
                stmt.execute();
                stmt.close();
                return true;
            }
        }catch (SQLException e){}
        return false;
    }

    public void receiveMyMedia(String filter){
        try {
            if(connection != null&&checkLibrary(filter)&&!connection.isClosed()) {
                String query = "select * from MyMedia;";
                if (filter != "empty") {
                    query = "select * from MyMedia where "+filter+"=true";
                }
                PreparedStatement stmt = connection.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    if(filter.equals("MusicLibrary")) {
                        MusicLibrary.getInstance().getMusicLibrary().add(new MyMedia(rs.getString("Title"), rs.getString("Artist"),
                                rs.getString("Album"), rs.getString("Genre"), rs.getString("Path")
                                , rs.getString("Length"), rs.getString("Year"), rs.getString("Image")));
                    }
                    else if(filter.equals("VideoLibrary")){
                        VideoLibrary.getInstance().getVideoLibrary().add(new MyMedia(rs.getString("Title"), rs.getString("Artist"),
                                rs.getString("Album"), rs.getString("Genre"), rs.getString("Path")
                                , rs.getString("Length"), rs.getString("Year"), rs.getString("Image")));
                    }
                }
                stmt.close();
                rs.close();
                return ;
            }

        }catch (SQLException e){}
    }
    public void receiveMediaInPlaylist(String name){
        try {
            if(connection != null&&!connection.isClosed() && isPresent("Name",name,"Playlist")&&isPresent("Name",name,"MyMediaPlaylist")) {
                String query = "select * " +
                        "from MyMedia,MyMediaPlaylist " +
                        "where MyMedia.Path=MyMediaPlaylist.Path and MyMediaPlaylist.Name=?" +
                        ";";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1,name);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    PlaylistCollection.getInstance().getPlayListsCollections().get(PlaylistCollection.getInstance().returnPlaylist(name)).add(
                            (new MyMedia(rs.getString("Title"), rs.getString("Artist"),
                            rs.getString("Album"), rs.getString("Genre"), rs.getString("Path")
                            , rs.getString("Length"), rs.getString("Year"), rs.getString("Image"))));
                }
                stmt.close();
                rs.close();
            }

        }catch (SQLException e){}
    }
    public void receivePlaylist(){
        try {
            if(connection != null&&!connection.isClosed()) {
                String query = "select * from Playlist;";
                PreparedStatement stmt = connection.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String name=rs.getString("Name");
                    Playlist newPlaylist = new Playlist(name,rs.getString("Image"),rs.getInt("Songs"),rs.getString("TotalDuration"));
                    newPlaylist.setInitialized(true);
                    PlaylistCollection.getInstance().addPlaylist(newPlaylist);
                }
                stmt.close();
                rs.close();
            }

        }catch (SQLException e){}
    }
    public void receiveRecentMedia(){
        try {
            if(connection != null&&!connection.isClosed()) {
                String query = "select * " +
                        "from MyMedia,RecentMedia " +
                        "where MyMedia.Path=RecentMedia.Path " +
                        ";";
                PreparedStatement stmt = connection.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String t=rs.getString("Title");
                    Home.getInstance().addToRecentMediaBis(new MyMedia(t, rs.getString("Artist"),
                            rs.getString("Album"), rs.getString("Genre"), rs.getString("Path")
                            , rs.getString("Length"), rs.getString("Year"), rs.getString("Image")));
                }
                stmt.close();
                rs.close();
                return ;
            }

        }catch (SQLException e){}
    }
    public void receivePlayqueue(){
        try {
            if(connection != null&&!connection.isClosed()) {
                String query = "select * " +
                        "from MyMedia,Playqueue " +
                        "where MyMedia.Path=Playqueue.Path " +
                        ";";
                PreparedStatement stmt = connection.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();
                ArrayList<MyMedia> myMedia = new ArrayList<>();
                while (rs.next()) {
                     PlayQueue.getInstance().setList(new MyMedia(rs.getString("Title"), rs.getString("Artist"),
                            rs.getString("Album"), rs.getString("Genre"), rs.getString("Path")
                            , rs.getString("Length"), rs.getString("Year"), rs.getString("Image")));
                }
                stmt.close();
                rs.close();
            }

        }catch (SQLException e){}
        return ;
    }


    public boolean deleteAll(String tab){
        try {
            if(connection != null&&checkTable(tab)&&!connection.isClosed()){
                PreparedStatement stmt=connection.prepareStatement("DELETE FROM "+tab+";");
                stmt.execute();
                stmt.close();
                return true;
            }
        }catch (SQLException e){}
        return false;
    }

    public boolean deleteMedia(String pathMymedia, String tab){
        try {
            if(connection != null&&checkTable(tab)&&pathMymedia!=null && !connection.isClosed()&&isPresent("Path",pathMymedia,tab)){
                //deleteMediaPlaylist(pathMymedia,"Path");
                PreparedStatement stmt=connection.prepareStatement("DELETE FROM "+tab+" WHERE Path=?;");
                stmt.setString(1,pathMymedia);
                stmt.execute();
                stmt.close();
                return true;
            }
        }catch (SQLException e){}
        return false;
    }

    public boolean deleteAllLibrary(String Library){
        try {
            if(connection != null&&checkLibrary(Library)){
                PreparedStatement stmt=connection.prepareStatement("DELETE FROM MyMedia WHERE "+Library+"=1");
                stmt.execute();
                stmt.close();
                return true;
            }
        }catch (SQLException e){ }
        return false;
    }
    public boolean deletePlaylist(String name){
        try {
            if(connection != null&&name!=null && !connection.isClosed()){
                deleteMediaPlaylist(name,"Name");
                PreparedStatement stmt=connection.prepareStatement("DELETE FROM Playlist WHERE Name=?;");
                stmt.setString(1,name);
                stmt.execute();
                stmt.close();
                return true;
            }
        }catch (SQLException e){}
        return false;
    }

    public  boolean deleteMediaPlaylist(String element,String Type){
        try {
            if(connection != null&&element!=null && !connection.isClosed()){

                PreparedStatement stmt=connection.prepareStatement("DELETE FROM MyMediaPlaylist WHERE "+Type+"=?;");
                stmt.setString(1,element);
                stmt.execute();
                stmt.close();
                return true;
            }
        }catch (SQLException e){}
        return false;
    }



    public void createTableMyMedia(){
        try {
            String query =
                    "CREATE TABLE IF NOT EXISTS MyMedia(Path VARCHAR(255)," +
                            "Title VARCHAR(100)," +
                            "Artist VARCHAR(100)," +
                            "Genre VARCHAR(100)," +
                            "Album VARCHAR(100)," +
                            "Image VARCHAR(255)," +
                            "Length VARCHAR(10)," +
                            "Year VARCHAR(10)," +
                            "MusicLibrary INTEGER,"+
                            "VideoLibrary INTEGER,"+
                            "PRIMARY KEY (Path));";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        }catch (SQLException e){}


    }
    public void createTableRecentMedia(){
        try {
            String query =
                    "CREATE TABLE IF NOT EXISTS RecentMedia(Path VARCHAR(255)," +
                            "FOREIGN KEY (Path) REFERENCES MyMedia(Path),"+
                            "PRIMARY KEY (Path));";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        }catch (SQLException e){}


    }
    public void createTableMediaMyMediaPlaylist(){
        try {
            String query =
                    "CREATE TABLE IF NOT EXISTS MyMediaPlaylist(Key INTEGER, Path VARCHAR(255)," +
                            "Name VARCHAR(100)," +
                            "FOREIGN KEY (Path) REFERENCES MyMedia(path),"+
                            "FOREIGN KEY (Name) REFERENCES Playlist(Name),"+
                            "PRIMARY KEY (Key));";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        }catch (SQLException e){}


    }
    public void createTablePlaylist(){
        try {
            String query =
                    "CREATE TABLE IF NOT EXISTS Playlist(Name VARCHAR(100) NOT NULL," +
                            "Image VARCHAR(255)," +
                            "Songs INT,"+
                            "TotalDuration VARCHAR(10),"+
                            "PRIMARY KEY (Name));";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        }catch (SQLException e){}


    }
    public void createTablePlayqueue(){
        try {
            String query =
                    "CREATE TABLE IF NOT EXISTS Playqueue(Key INTEGER, Path VARCHAR(255)," +
                            "FOREIGN KEY (Path) REFERENCES MyMedia(path),"+
                            "PRIMARY KEY (Key));";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        }catch (SQLException e){}
    }



    public void createTableTheme(){
        try {
            String query =
                    "CREATE TABLE IF NOT EXISTS AppTheme(Key VARCHAR(3)," +
                            "Theme VARCHAR(100),"+
                            "PRIMARY KEY (Key));";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        }catch (SQLException e){}
    }
    public boolean changeTheme(String theme){
        try {
            if(connection != null &&!connection.isClosed()) {
                PreparedStatement stmt = connection.prepareStatement(
                        "UPDATE AppTheme " +
                                "SET Theme=? " +
                                "WHERE Key=Key;");
                stmt.setString(1, theme);
                stmt.execute();
                stmt.close();
                return true;
            }
        }catch (SQLException e) {}
        return false;
    }
    public boolean initTheme(){
        try {
            if(connection != null &&!connection.isClosed()&&!isPresent("Key", "Key","AppTheme")) {
                PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO AppTheme VALUES(?, ?);");
                stmt.setString(1,"Key");
                stmt.setString(2, "dark");
                stmt.execute();
                stmt.close();
                return true;
            }
        }catch (SQLException e) {}
        return false;
    }
    public void receiveTheme(){
        try {
            if(connection != null&&!connection.isClosed()) {
                String query = "select * " +
                        "from AppTheme " +
                        "where Key=Key" +
                        ";";
                PreparedStatement stmt = connection.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    Settings.theme=rs.getString("Theme");
                }
                stmt.close();
                rs.close();
            }

        }catch (SQLException e){}
    }


    public void createTableEqualizer(){
        try {
            String query =
                    "CREATE TABLE IF NOT EXISTS Equalizer(" +
                            "Hz32 INTEGER," +
                            "Hz64 INTEGER," +
                            "Hz125 INTEGER," +
                            "Hz250 INTEGER," +
                            "Hz500 INTEGER," +
                            "Hz1000 INTEGER," +
                            "Hz2000 INTEGER," +
                            "Hz4000 INTEGER," +
                            "Hz8000 INTEGER,"+
                            "Hz16000 INTEGER," +
                            "Key VARCHAR(3));" ;
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        }catch (SQLException e){}
    }
    public boolean initEqualizer(){
        try {
            if(connection != null &&!connection.isClosed()&&!isPresent("Key","key","Equalizer")) {
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO Equalizer VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
                stmt.setInt(1,0);
                stmt.setInt(2,0);
                stmt.setInt(3,0);
                stmt.setInt(4,0);
                stmt.setInt(5,0);
                stmt.setInt(6,0);
                stmt.setInt(7,0);
                stmt.setInt(8,0);
                stmt.setInt(9,0);
                stmt.setInt(10,0);
                stmt.setString(11,"key");
                stmt.execute();
                stmt.close();
                return true;
            }
        }catch (SQLException e) {}
        return false;
    }
    public boolean setEqualizer(int []vett){
        try {
            if(connection != null &&!connection.isClosed()) {
                PreparedStatement stmt = connection.prepareStatement("UPDATE Equalizer SET " +
                        "Hz32=?," +
                        "Hz64=?," +
                        "Hz125=?," +
                        "Hz250=?," +
                        "Hz500=?," +
                        "Hz1000=?," +
                        "Hz2000=?," +
                        "Hz4000=?," +
                        "Hz8000=?," +
                        "Hz16000=?  WHERE Key=key ;");
                stmt.setInt(1,vett[0]);
                stmt.setInt(2,vett[1]);
                stmt.setInt(3,vett[2]);
                stmt.setInt(4,vett[3]);
                stmt.setInt(5,vett[4]);
                stmt.setInt(6,vett[5]);
                stmt.setInt(7,vett[6]);
                stmt.setInt(8,vett[7]);
                stmt.setInt(9,vett[8]);
                stmt.setInt(10,vett[9]);
                stmt.execute();
                stmt.close();
                return true;
            }
        }catch (SQLException e) {}
        return false;
    }
    public boolean getEqualizer(){
        try {
            String query = "select * from Equalizer where Key=key;";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            int []settings=new int[10];
            if(rs.next()){
                settings[0]=rs.getInt("Hz32");
                settings[1]=rs.getInt("Hz64");
                settings[2]=rs.getInt("Hz125");
                settings[3]=rs.getInt("Hz250");
                settings[4]=rs.getInt("Hz500");
                settings[5]=rs.getInt("Hz1000");
                settings[6]=rs.getInt("Hz2000");
                settings[7]=rs.getInt("Hz4000");
                settings[8]=rs.getInt("Hz8000");
                settings[9]=rs.getInt("Hz16000");
            }
            AudioEqualizer.getInstance().getPresetsValues().remove(AudioEqualizer.getInstance().getPresetsNames().size()-1);
            AudioEqualizer.getInstance().getPresetsValues().add(settings);
            return true;
        }catch (SQLException e){}
        return false;
    }


    //END FUNCTION
}


