class SudokuFileBrowser extends JFileChooser{

    SudokuFileBrowser(String dialogTitle){
	setDialogTitle(dialogTitle);
	FileNameExtensionFilter f = new FileNameExtensionFilter("Txt files", "txt");
	setFileFilter(f);
    }	
}