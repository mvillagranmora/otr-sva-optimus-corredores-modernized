SET SCHEMA "BSCL";


-- el 06-01-2015 se agrega en tabla TipoOperacion campo que determinará si la operación corresponde 
-- a las usadas en la pantalla de ingreso/retiro de título
	alter table "BSCL.Plataforma.BD::INS.TipoOperacion"
	add ("EsIngresoEgresoTitulo" 		NVARCHAR(1)			default 'N');
	
--CELZORRO-266 - JSanhueza_EXT
	alter table "BSCL.Custodia.BD::CUS.EventoCapital"
	add ("IdBoveda" 		NVARCHAR(3)			default '');
	
