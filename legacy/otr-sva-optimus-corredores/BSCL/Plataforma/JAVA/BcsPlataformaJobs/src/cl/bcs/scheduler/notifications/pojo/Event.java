package cl.bcs.scheduler.notifications.pojo;

public class Event
{
	public int getIdEvent() {
		return idEvent;
	}
	public void setIdEvent(int idEvent) {
		this.idEvent = idEvent;
	}
	public String getProcess() {
		return process;
	}
	public void setProcess(String process) {
		this.process = process;
	}
	public int getIdConfigurationEvent()
	{
		return IdConfigurationEvent;
	}
	public void setIdConfigurationEvent(int idConfigurationEvent)
	{
		this.IdConfigurationEvent = idConfigurationEvent;
	}
	public int idEvent;
	public String process;
	public int IdConfigurationEvent;
	public int notificationsId;
	public boolean isGroup;
	public boolean isGroup() {
		return isGroup;
	}
	public void setGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}
	public int getNotificationsId() {
		return notificationsId;
	}
	public void setNotificationsId(int notificationsId) {
		this.notificationsId = notificationsId;
	}
 

}
