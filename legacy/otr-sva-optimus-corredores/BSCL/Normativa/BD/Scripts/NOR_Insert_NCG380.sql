set schema "BSCL";


--Insert Consolida Clientes en Agenda
SET SCHEMA "BSCL";

DROP TABLE #v_IdProgramTable;

CREATE LOCAL TEMPORARY TABLE #v_IdProgramTable
(
  "NombreParametro"   NVARCHAR(100),      
   "Valor"   NVARCHAR(100)
);

INSERT INTO #v_IdProgramTable VALUES ('IdEmpresa','1');


call "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda" (1,1,'NOR','ConsolidacionCli','N','**','*','**',8,30,'S','N','N','S',#v_IdProgramTable,?,?,?,?,?,?);

