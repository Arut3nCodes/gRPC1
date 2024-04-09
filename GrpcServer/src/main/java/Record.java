import java.util.Objects;

public class Record {

    private  int itemId;
    private String nameOfProduct;
    private double price;
    private int numberOfItems;
    private String nameOfImage;

    public Record() {
    }

    public Record(int itemId, String nameOfProduct, double price, int numberOfItems, String nameOfImage) {
        this.itemId = itemId;
        this.nameOfProduct = nameOfProduct;
        this.price = price;
        this.numberOfItems = numberOfItems;
        this.nameOfImage = nameOfImage;
    }

    public String getNameOfProduct() {
        return nameOfProduct;
    }

    public void setNameOfProduct(String nameOfProduct) {
        this.nameOfProduct = nameOfProduct;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getNumberOfItems() {
        return numberOfItems;
    }

    public void setNumberOfItems(int numberOfItems) {
        this.numberOfItems = numberOfItems;
    }

    public String getNameOfImage() {
        return nameOfImage;
    }

    public void setNameOfImage(String nameOfImage) {
        this.nameOfImage = nameOfImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Record record = (Record) o;
        return itemId == record.itemId && Double.compare(price, record.price) == 0 && numberOfItems == record.numberOfItems && Objects.equals(nameOfProduct, record.nameOfProduct) && Objects.equals(nameOfImage, record.nameOfImage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, nameOfProduct, price, numberOfItems, nameOfImage);
    }

    @Override
    public String toString() {
        return "Record{" +
                "nameOfProduct='" + nameOfProduct + '\'' +
                ", price=" + price +
                ", numberOfItems=" + numberOfItems +
                ", nameOfImage='" + nameOfImage + '\'' +
                '}';
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
        public static Builder newBuilder() {
            return new Builder();
        }

        public static class Builder {
            private int itemId;
            private String nameOfProduct;
            private double price;
            private int numberOfItems;
            private String nameOfImage;

            private Builder() {}

            public Builder setItemId(int itemId) {
                this.itemId = itemId;
                return this;
            }

            public Builder setNameOfProduct(String nameOfProduct) {
                this.nameOfProduct = nameOfProduct;
                return this;
            }

            public Builder setPrice(double price) {
                this.price = price;
                return this;
            }

            public Builder setNumberOfItems(int numberOfItems) {
                this.numberOfItems = numberOfItems;
                return this;
            }

            public Builder setNameOfImage(String nameOfImage) {
                this.nameOfImage = nameOfImage;
                return this;
            }

            public Record build() {
                return new Record(itemId, nameOfProduct, price, numberOfItems, nameOfImage);
            }
        }
}
