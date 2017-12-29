/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.dspace.browse.BrowsableDSpaceObject;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.handle.factory.HandleServiceFactory;
import org.hibernate.proxy.HibernateProxyHelper;

/**
 * Class representing bitstreams stored in the DSpace system.
 * <P>
 * When modifying the bitstream metadata, changes are not reflected in the
 * database until <code>update</code> is called. Note that you cannot alter
 * the contents of a bitstream; you need to create a new bitstream.
 * 
 * @author Robert Tansley
 * @version $Revision$
 */
@Entity
@Table(name="bitstream")
public class Bitstream extends DSpaceObject implements DSpaceObjectLegacySupport, BrowsableDSpaceObject
{
    /** log4j logger */
    private static Logger log = Logger.getLogger(Bitstream.class);
    
    @Column(name="bitstream_id", insertable = false, updatable = false)
    private Integer legacyId;

    @Column(name = "sequence_id")
    private Integer sequenceId = -1;

    @Column(name = "checksum", length = 64)
    private String checksum;

    @Column(name = "checksum_algorithm", length = 32)
    private String checksumAlgorithm;

    @Column(name = "size_bytes")
    private long sizeBytes;

    @Column(name = "deleted")
    private boolean deleted = false;

    @Column(name = "internal_id", length = 256)
    private String internalId;

    @Column(name = "store_number")
    private int storeNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bitstream_format_id")
    private BitstreamFormat bitstreamFormat;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "bitstreams")
    private List<Bundle> bundles = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, mappedBy="logo")
    private Community community;

    @OneToOne(fetch = FetchType.LAZY, mappedBy="logo")
    private Collection collection;

    @Transient
    private transient BitstreamService bitstreamService;


    /**
     * Protected constructor, create object using:
     * {@link org.dspace.content.service.BitstreamService#create(Context, Bundle, InputStream)}
     * or
     * {@link org.dspace.content.service.BitstreamService#create(Context, InputStream)}
     */
    protected Bitstream()
    {
    }

    /**
     * Get the sequence ID of this bitstream
     * 
     * @return the sequence ID
     */
    public int getSequenceID()
    {
        if(sequenceId == null)
        {
            return -1;
        }else{
            return sequenceId;
        }
    }

    /**
     * Set the sequence ID of this bitstream
     * 
     * @param sid
     *            the ID
     */
    public void setSequenceID(int sid)
    {
        sequenceId = sid;
        setMetadataModified();
        addDetails("SequenceID");
    }

    /**
     * Get the name of this bitstream - typically the filename, without any path
     * information
     * 
     * @return the name of the bitstream
     */
    @Override
    public String getName(){
        return getBitstreamService().getMetadataFirstValue(this, MetadataSchema.DC_SCHEMA, "title", null, Item.ANY);
    }

    /**
     * Set the name of the bitstream
     * 
     * @param context context
     * @param n
     *            the new name of the bitstream
     * @throws SQLException if database error
     */
    public void setName(Context context, String n) throws SQLException {
        getBitstreamService().setMetadataSingleValue(context, this, MetadataSchema.DC_SCHEMA, "title", null, null, n);
    }

    /**
     * Get the source of this bitstream - typically the filename with path
     * information (if originally provided) or the name of the tool that
     * generated this bitstream
     * 
     * @return the source of the bitstream
     */
    public String getSource()
    {
        return getBitstreamService().getMetadataFirstValue(this, MetadataSchema.DC_SCHEMA, "source", null, Item.ANY);
    }

    /**
     * Set the source of the bitstream
     * 
     * @param context context
     * @param n
     *            the new source of the bitstream
     * @throws SQLException if database error
     */
    public void setSource(Context context, String n) throws SQLException {
        getBitstreamService().setMetadataSingleValue(context, this, MetadataSchema.DC_SCHEMA, "source", null, null, n);
    }

    /**
     * Get the description of this bitstream - optional free text, typically
     * provided by a user at submission time
     * 
     * @return the description of the bitstream
     */
    public String getDescription()
    {
        return getBitstreamService().getMetadataFirstValue(this, MetadataSchema.DC_SCHEMA, "description", null, Item.ANY);
    }

    /**
     * Set the description of the bitstream
     * 
     * @param context context
     * @param n
     *            the new description of the bitstream
     * @throws SQLException if database error
     */
    public void setDescription(Context context, String n) throws SQLException {
        getBitstreamService().setMetadataSingleValue(context, this, MetadataSchema.DC_SCHEMA, "description", null, null, n);
    }

    /**
     * Get the checksum of the content of the bitstream, for integrity checking
     * 
     * @return the checksum
     */
    public String getChecksum()
    {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    /**
     * Get the algorithm used to calculate the checksum
     * 
     * @return the algorithm, e.g. "MD5"
     */
    public String getChecksumAlgorithm()
    {
        return checksumAlgorithm;
    }

    public void setChecksumAlgorithm(String checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
    }

    /**
     * Get the size of the bitstream
     * 
     * @return the size in bytes
     */
    public long getSize()
    {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    /**
     * Get the user's format description. Returns null if the format is known by
     * the system.
     * 
     * @return the user's format description.
     */
    public String getUserFormatDescription()
    {
        return getBitstreamService().getMetadataFirstValue(this, MetadataSchema.DC_SCHEMA, "format", null, Item.ANY);
    }

    protected BitstreamFormat getBitstreamFormat()
    {
        return bitstreamFormat;
    }

    /**
     * Get the format of the bitstream
     * 
     * @param context context
     * @return the format of this bitstream
     * @throws SQLException if database error
     */
    public BitstreamFormat getFormat(Context context) throws SQLException
    {
        return getBitstreamService().getFormat(context, this);
    }
    
    public BitstreamFormat getFormat() throws SQLException
    {
    	Context context = new Context();
        return getBitstreamService().getFormat(context, this);
    }

    void setFormat(BitstreamFormat bitstreamFormat) {
        this.bitstreamFormat = bitstreamFormat;
        setModified();
    }

    /**
     * Bitstreams are only logically deleted (via a flag in the database).
     * This method allows us to verify is the bitstream is still valid
     *
     * @return true if the bitstream has been deleted
     * @throws SQLException if database error
     */
    public boolean isDeleted() throws SQLException
    {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Get the bundles this bitstream appears in
     * 
     * @return array of <code>Bundle</code> s this bitstream appears in
     * @throws SQLException if database error
     */
    public List<Bundle> getBundles() throws SQLException
    {
        return bundles;
    }

    void setBundles(List<Bundle> bundles) {
        this.bundles = bundles;
    }


    /**
     * return type found in Constants
     * 
     * @return int Constants.BITSTREAM
     */
    @Override
    public int getType()
    {
        return Constants.BITSTREAM;
    }

    public Collection getCollection() {
        return collection;
    }

    public Community getCommunity() {
        return community;
    }

    /**
     * Get the asset store number where this bitstream is stored
     * 
     * @return the asset store number of the bitstream
     */
    public int getStoreNumber() {
        return storeNumber;
    }

    /**
     * Set the asset store number where this bitstream is stored
     *
     * @param storeNumber asset store number of the bitstream
     */
    public void setStoreNumber(int storeNumber) {
        this.storeNumber = storeNumber;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    /*
        Getters & setters which should be removed on the long run, they are just here to provide all getters & setters to the item object
     */


    /**
     * Set the user's format description. This implies that the format of the
     * bitstream is uncertain, and the format is set to "unknown."
     *
     * @param context
     *     The relevant DSpace Context.
     * @param desc
     *     the user's description of the format
     * @throws SQLException if database error
     */
    public void setUserFormatDescription(Context context, String desc) throws SQLException
    {
        getBitstreamService().setUserFormatDescription(context, this, desc);
    }

    /**
     * Get the description of the format - either the user's or the description
     * of the format defined by the system.
     *
     * @param context context
     * @return a description of the format.
     * @throws SQLException if database error
     */
    public String getFormatDescription(Context context) throws SQLException
    {
        return getBitstreamService().getFormatDescription(context, this);
    }

    /**
     * Set the format of the bitstream. If the user has supplied a type
     * description, it is cleared. Passing in <code>null</code> sets the type
     * of this bitstream to "unknown".
     *
     * @param context context
     * @param f
     *            the format of this bitstream, or <code>null</code> for
     *            unknown
     * @throws SQLException if database error
     */
    public void setFormat(Context context, BitstreamFormat f) throws SQLException
    {
        getBitstreamService().setFormat(context, this, f);
    }

    @Override
    public Integer getLegacyId() {
        return legacyId;
    }

    public BitstreamService getBitstreamService() {
        if(bitstreamService == null)
        {
            bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();
        }
        return bitstreamService;
    }

    /**
     * Return <code>true</code> if <code>other</code> is the same Bitstream
     * as this object, <code>false</code> otherwise
     *
     * @param other
     *            object to compare to
     *
     * @return <code>true</code> if object passed in represents the same
     *         collection as this object
     */
     @Override
     public boolean equals(Object other)
     {
         if (other == null)
         {
             return false;
         }
         Class<?> objClass = HibernateProxyHelper.getClassWithoutInitializingProxy(other);
         if (this.getClass() != objClass)
         {
             return false;
         }
         final Bitstream otherBitstream = (Bitstream) other;
         if (!this.getID().equals(otherBitstream.getID()))
         {
             return false;
         }

         return true;
     }

     @Override
     public int hashCode()
     {
         int hash = 5;
         hash += 73 * hash + getType();
         hash += 73 * hash + getID().hashCode();
         return hash;
     }

	/**
	 * Add date for bitstream granted (used into the use case for license grant the {@link LicenseUtils#grantLicense(Context, Item, String, String)}
	 * 
	 * @param context
     *            the dspace context
	 * @param acceptanceDate the granted date
	 * @throws SQLException
	 */
	public void setAcceptanceDate(Context context, DCDate acceptanceDate) throws SQLException {
		getBitstreamService().setMetadataSingleValue(context, this, "dcterms", "accessRights", null, null, acceptanceDate.toString());
	}

}
    
    public void setMD5Value(Context context, String valueMD5) {
        this.checksum = valueMD5;
    }

	@Override
	public String getTypeText() {
		return Constants.typeText[Constants.BITSTREAM];
	}

	@Override
	public DSpaceObjectService getDSpaceObjectService() {
		return getBitstreamService();
	}

	@Override
	public boolean haveHierarchy() {
		return true;
	}

	@Override
	public Map<String, Object> getExtraInfo() {
		return new HashMap<>();
	}

	@Override
	public boolean isArchived() {
		return false;
	}

	@Override
	public List<IMetadataValue> getMetadata(String schema, String element, String qualifier, String lang) {
		return getBitstreamService().getMetadata(this, schema, element, qualifier, lang);
	}

	@Override
	public String getMetadata(String field) {
		return getBitstreamService().getMetadata(this, field);
	}

	@Override
	public boolean isDiscoverable() {
		return false;
	}

	@Override
	public String findHandle(Context context) throws SQLException {
		return HandleServiceFactory.getInstance().getHandleService().findHandle(context, this);
	}

	@Override
	public BrowsableDSpaceObject getParentObject() {		
		Context context = new Context();
		try {
			return (BrowsableDSpaceObject)(getBitstreamService().getParentObject(context, this));
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public String getMetadataFirstValue(String schema, String element, String qualifier, String language) {
		return getBitstreamService().getMetadataFirstValue(this, schema, element, qualifier, language);
	}

	@Override
	public Date getLastModified() {
		return new Date();
	}
}