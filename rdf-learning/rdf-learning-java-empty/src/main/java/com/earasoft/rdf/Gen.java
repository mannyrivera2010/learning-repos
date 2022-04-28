package fam;

import java.util.Queue;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Gen {

    static enum PersonColor {
        RED, BLUE
    }

    static enum PersonState {
        PARENT, COUPLED, SINGLE
    }

    static class Family {
        Person blueParent;
        Person redParent;
        
        List<Person> blueKids;
        List<Person> redKids;
    }

    static class World {
        Integer counter = 0;

        Map<PersonColor, Integer> personColorCounter = new HashMap<PersonColor, Integer>();

        Random rand = new Random();

        // state - id - person
        Map<Gen.PersonState, Map<Integer, Person>> personStateMap = new HashMap<Gen.PersonState, Map<Integer, Person>>();

        public World() {
            personStateMap.put(Gen.PersonState.PARENT, new HashMap<Integer, Person>());
            personStateMap.put(Gen.PersonState.COUPLED, new HashMap<Integer, Person>());
            personStateMap.put(Gen.PersonState.SINGLE, new HashMap<Integer, Person>());
        }

        /**
         * @return the personStateMap
         */
        public Map<Gen.PersonState, Map<Integer, Person>> getPersonStateMap() {
            return personStateMap;
        }

        /**
         * @param personStateMap
         *            the personStateMap to set
         */
        public void setPersonStateMap(Map<Gen.PersonState, Map<Integer, Person>> personStateMap) {
            this.personStateMap = personStateMap;
        }

        public void genPeople(int n) {
            PersonState personState = PersonState.SINGLE;
            for (int i = 0; i < n; i++) {
                genOnePerson(personState);
            }
        }

        public Queue<Person> getQueueForState(PersonState personState){
            Queue<Person> peopleQueue = new ArrayDeque<Person>();

            for (Person person : personStateMap.get(personState).values()) {
                peopleQueue.add(person);
            }
            
            return peopleQueue;
        }
        
        /**
         * Find Pairs
         */
        public void findPartners() {
            Queue<Person> peopleQueue = getQueueForState(PersonState.SINGLE);
            
            while (!peopleQueue.isEmpty()) {
                Person person = peopleQueue.poll();
                person.findPartner();
            }

        }

        public void pairPeople(Person left, Person right) {
            Person leftPerson = personStateMap.get(Gen.PersonState.SINGLE).get(left.id);
            Person rightPerson = personStateMap.get(Gen.PersonState.SINGLE).get(right.id);

            if (leftPerson == null || rightPerson == null) {
                // person state change
                return;
            }

            leftPerson.setState(Gen.PersonState.COUPLED);
            rightPerson.setState(Gen.PersonState.COUPLED);

            personStateMap.get(Gen.PersonState.COUPLED).put(left.id, leftPerson);
            personStateMap.get(Gen.PersonState.COUPLED).put(right.id, rightPerson);

            personStateMap.get(Gen.PersonState.SINGLE).remove(left.id);
            personStateMap.get(Gen.PersonState.SINGLE).remove(right.id);
        }

        /**
         * @param personState
         */
        private Person genOnePerson(PersonState personState) {
            counter += 1;
            int rand_int1 = rand.nextInt(10);

            PersonColor color;

            if (rand_int1 >= 5) {
                color = PersonColor.BLUE;
            } else {
                color = PersonColor.RED;
            }

            if (!personColorCounter.containsKey(color)) {
                personColorCounter.put(color, 0);
            }

            personColorCounter.put(color, personColorCounter.get(color) + 1);

            Person person = new Person(this, counter, color, PersonState.SINGLE);

            personStateMap.get(personState).put(counter, person);
            
            return person;
        }

        /**
         * Get State Counter Map
         * @return
         */
        public Map<PersonState, Integer> getStateCounterMap() {
            Map<PersonState, Integer> map = new TreeMap<PersonState, Integer>();

            for (Map.Entry<PersonState, Map<Integer, Person>> entry : personStateMap.entrySet()) {
                map.put(entry.getKey(), entry.getValue().size());
            }

            return map;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("World [counter=").append(counter).append(", personColorCounter=").append(personColorCounter)
                    .append(", getStateCounterMap()=").append(getStateCounterMap()).append("]");
            return builder.toString();
        }

    }

    static class Person {
        final World world;
        final Integer id;
        final PersonColor color;
        PersonState state;

        public Person(World world, Integer id, PersonColor color, PersonState state) {
            this.world = world;
            this.id = id;
            this.color = color;
            this.state = state;
        }

        public boolean findPartner() {
            Queue<Person> peopleQueue = new ArrayDeque<Person>();

            for (Person person : world.getPersonStateMap().get(Gen.PersonState.SINGLE).values()) {
                peopleQueue.add(person);
            }

            while (!peopleQueue.isEmpty()) {

                Person person = peopleQueue.poll();

                // can't pair by it self
                if (this.id != null && this.id.equals(person.id)) {
                    continue;
                }
                // can't pair with same color
                if (this.color != null && this.color.equals(person.color)) {
                    continue;
                }

                if (!this.state.equals(person.state)) {
                    continue;
                }

                world.pairPeople(this, person);

                System.out.println(this + " - " + person + " - " + world.getStateCounterMap());

                break;

            }
            return false;
        }

        /**
         * @param state
         *            the state to set
         */
        public void setState(PersonState state) {
            this.state = state;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Person other = (Person) obj;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Person [id=").append(id).append(", color=").append(color).append(", state=").append(state)
                    .append("]");
            return builder.toString();
        }

    }

    public static void main(String[] args) {
        World world = new World();

        world.genPeople(100);

        world.findPartners();

        System.out.println(world);
    }

}
