package fi.lbd.mobile.messaging;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import fi.lbd.mobile.messaging.messageobjects.MessageObject;

/**
 * Created by Ossi on 14.1.2015.
 */
public class MessageObjectRepository {
    private static MessageObjectRepository singleton;

    private MessageObjectRepository() {}

    private List<MessageObject> objects;

    public static void initialize() {
        if (MessageObjectRepository.singleton == null) {
            MessageObjectRepository.singleton = new MessageObjectRepository();
        }
    }

    public static synchronized MessageObjectRepository get() {
        return MessageObjectRepository.singleton;
    }

    public void setObjects(List<MessageObject> newObjects){
        this.objects = newObjects;
    }

    public List<MessageObject> getObjects(){
        return this.objects;
    }

    // TODO: More efficient way to comparison (using sets?)
    public static boolean areMessageListsIdentical(List<MessageObject> oldMessageObjects,
                                             List<MessageObject> newMessageObjects){
        if(newMessageObjects == null || oldMessageObjects == null){
            return false;
        }
        boolean areIdentical = true;
        for(MessageObject newObject : newMessageObjects){
            int iterator = 0;
            for(MessageObject oldObject : oldMessageObjects){
                if(oldObject.equals(newObject)){
                    break;
                }
                ++iterator;
            }
            if(iterator == oldMessageObjects.size()){
                areIdentical = false;
                break;
            }
        }
        return areIdentical;
    }

    public static List<MessageObject> sortMessagesByTimestamp(List<MessageObject> messageObjects){
        Collections.sort(messageObjects, new Comparator<MessageObject>() {
            @Override
            public int compare(MessageObject o1, MessageObject o2) {
                long t1 = o1.getTimestamp();
                long t2 = o2.getTimestamp();
                if(t1 == t2){
                    return 0;
                }
                return t1 < t2 ? 1 : -1;
            }
        });
        return messageObjects;
    }

    public void deleteMessage(String deleteId){
        for(Iterator iterator = this.objects.iterator(); iterator.hasNext();){

            if(((MessageObject)iterator.next()).getId().equals(deleteId)){
                iterator.remove();
                break;
            }
        }
    }
}
