SET SCHEMA BSCL;

ALTER TABLE "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" ADD (
	"PermiteDesactivarOptCliente" NVARCHAR(1) DEFAULT 'N'
);