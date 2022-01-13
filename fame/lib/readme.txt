UIFame4ALC is one of the Fame releases used for computing uniform interpolants for ALC TBoxes.

UIFame4ALC.jar is a Java library which can be integrated into other systems for forgetting or related tasks. To use the tool, you should:

1. New a Fame instance in your project (do not forget import the Fame class), e.g., Fame fame = new Fame();
2. Call the FameRC(r_sig, c_sig, onto) method. e.g., fame.FameRC(r_sig, c_sig, onto), where r_sig is a set of OWLObjectProperty to be forgotten, c_sig is a set of OWLClass to be forgotten, and onto is the OWL ontology from which the names in r_sig and c_sig are eliminated.     

UIFame4ALC_ui.jar is an executable .jar file which can be used as a standalone tool via a user interface. To run UIFame4ALC_ui.jar, please type in the command line 'java -jar UIFame4ALC_ui.jar'. To perform forgetting, you should first load an OWL ontology from a local directory, then select manually the concept and role names you want to forget (role names in upper frame and cocnept names in lower frame), and finally click the 'forget' button. The forgetting solution will be accordingly displayed in the right frame.



  