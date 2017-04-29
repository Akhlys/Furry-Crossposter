package klaue.furrycrossposter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ImageInfo implements ChangeListener {
	public enum Type {SKETCH("Sketch"), DIGITAL("Digital"), TRADITIONAL("Traditional");
		private final String display;
	    private Type(String s) {
	        display = s;
	    }
	    @Override
	    public String toString() {
	        return display;
	    }
    };
    
    public enum RatingSexual {NONE("None"), NUDITY_MOD("Moderate nudity"), NUDITY_EX("Explicit Nudity or Sex");
	    private final String display;
	    private RatingSexual(String s) {
	        display = s;
	    }
	    @Override
	    public String toString() {
	        return display;
	    }
    }
    
    public enum RatingViolence {NONE("None"), VIOLENCE_MOD("Mild violence"), VIOLENCE_EX("Strong Violence, Blood");
	    private final String display;
	    private RatingViolence(String s) {
	        display = s;
	    }
	    @Override
	    public String toString() {
	        return display;
	    }
	}
    
    public enum Gender {AMBIGUOUS("ambiguous_gender"), MALE("male"), FEMALE("female"), HERM("herm"), DICKGIRL("dickgirl"), CUNTBOY("cuntboy"),
    					MALEHERM("maleherm"), M2F("mtf"), F2M("ftm");
	    private final String tag;
	    private Gender(String s) {
	        tag = s;
	    }
	    public String getTag() {
	        return tag;
	    }
	    @Override
	    public String toString() {
	    	return getTag();
	    }
	}
	
	private Path imagePath = null;
	private Path thumbPath = null;
	
	private Type type = Type.DIGITAL;
	private RatingSexual sexualRating = RatingSexual.NONE;
	private RatingViolence violenceRating = RatingViolence.NONE;
	private boolean toScraps = false;
	private boolean friendsOnly = false;
	private boolean noNotification = false;
	private boolean unlisted = false;
	
	private String title = "";
	private String description = "";
	
	private ListenableTreeSet<Gender> genders = new ListenableTreeSet<Gender>();
	
	private ListenableTreeSet<String> speciesTags = new ListenableTreeSet<String>();
	private ListenableTreeSet<String> kinkTags = new ListenableTreeSet<String>();
	private ListenableTreeSet<String> otherTags = new ListenableTreeSet<String>();
	
	private ListenableTreeSet<String> folders = new ListenableTreeSet<String>();
	
	private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
	
	private boolean listChangeEventWaiting = false;
	
	public ImageInfo() {
		this.genders.addChangeListener(this);
		this.speciesTags.addChangeListener(this);
		this.kinkTags.addChangeListener(this);
		this.otherTags.addChangeListener(this);
		this.folders.addChangeListener(this);
	}
	
	public Path getImagePath() {
		return imagePath;
	}
	public void setImagePath(Path imagePath) {
		this.imagePath = imagePath;
		fireChangeEvent();
	}
	public Path getThumbPath() {
		return thumbPath;
	}
	public void setThumbPath(Path thumbPath) {
		this.thumbPath = thumbPath;
		fireChangeEvent();
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
		fireChangeEvent();
	}
	public boolean isToScraps() {
		return toScraps;
	}
	public void setToScraps(boolean toScraps) {
		this.toScraps = toScraps;
		fireChangeEvent();
	}
	public boolean isFriendsOnly() {
		return friendsOnly;
	}
	public void setFriendsOnly(boolean friendsOnly) {
		this.friendsOnly = friendsOnly;
		fireChangeEvent();
	}
	public boolean hasNoNotification() {
		return noNotification;
	}
	public void setNoNotification(boolean noNotification) {
		this.noNotification = noNotification;
		fireChangeEvent();
	}
	public boolean isUnlisted() {
		return unlisted;
	}
	public void setUnlisted(boolean unlisted) {
		this.unlisted = unlisted;
		fireChangeEvent();
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
		fireChangeEvent();
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
		fireChangeEvent();
	}
	public void setGender(Gender gender, boolean set) {
		if (set) {
			addGender(gender);
		} else {
			removeGender(gender);
		}
	}
	public void addGender(Gender gender) {
		this.genders.add(gender);
	}
	public void removeGender(Gender gender) {
		this.genders.remove(gender);
	}
	public TreeSet<Gender> getGenders() {
		return genders;
	}
	public TreeSet<String> getSpeciesTags() {
		return speciesTags;
	}
	public TreeSet<String> getOtherTags() {
		return otherTags;
	}
	public TreeSet<String> getKinkTags() {
		return kinkTags;
	}
	public TreeSet<String> getFolders() {
		return folders;
	}
	public RatingSexual getSexualRating() {
		return sexualRating;
	}
	public void setSexualRating(RatingSexual sexualRating) {
		this.sexualRating = sexualRating;
		fireChangeEvent();
	}
	public RatingViolence getViolenceRating() {
		return violenceRating;
	}
	public void setViolenceRating(RatingViolence violenceRating) {
		this.violenceRating = violenceRating;
		fireChangeEvent();
	}
	public void addChangeListener(ChangeListener c) {
		if (!this.listeners.contains(c)) this.listeners.add(c);
	}
	public void removeChangeListener(ChangeListener c) {
		this.listeners.remove(c);
	}
	
	private void fireChangeEvent() {
		ChangeEvent e = new ChangeEvent(this);
		for (ChangeListener listener : listeners) {
			listener.stateChanged(e);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// can happen multiple times in a row, as those are usually cleared and then right added to again,
		// only fire all 100 ms max
		if (!listChangeEventWaiting) {
			listChangeEventWaiting = true;
			new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// ignore
					}
					listChangeEventWaiting = false;
					fireChangeEvent();
				}
			}).start();
		}
	}
}
