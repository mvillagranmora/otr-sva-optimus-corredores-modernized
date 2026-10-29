 INSERT INTO  "BSCL.FondoMutuo.BD::FMM.FondosIndustria"(
 	"IdFondoIndustria"
 	,"RunFFMM"
	,"NombreFFMM" 
	,"RutSociedadAdmFFMM"
	,"SociedadAdmFFMM"
	,"IdEstado" 
	,"IdMoneda"
 )VALUES(
 	:IdFondoIndustria
 	,:RunFFMM
 	,:NombreFFMM
 	,:RutSociedadAdmFFMM
 	,:SociedadAdmFFMM
 	,'ACT'
 	,:IdMoneda
 );