package hkmu.wadd.dao;

import hkmu.wadd.exception.AttachmentNotFound;
import hkmu.wadd.exception.TicketNotFound;
import hkmu.wadd.model.Attachment;
import hkmu.wadd.model.Ticket;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class TicketService {
    @Resource
    private TicketRepository tRepo;

    @Resource
    private AttachmentRepository aRepo;

    @Transactional
    public List<Ticket> getTickets() {
        return tRepo.findAll();
    }

    @Transactional
    public Ticket getTicket(long id)
            throws TicketNotFound {
        Ticket ticket = tRepo.findById(id).orElse(null);
        if (ticket == null) {
            throw new TicketNotFound(id);
        }
        return ticket;
    }

    @Transactional
    public Attachment getAttachment(long ticketId, UUID attachmentId)
            throws TicketNotFound, AttachmentNotFound {
        Ticket ticket = tRepo.findById(ticketId).orElse(null);
        if (ticket == null) {
            throw new TicketNotFound(ticketId);
        }
        Attachment attachment = aRepo.findById(attachmentId).orElse(null);
        if (attachment == null) {
            throw new AttachmentNotFound(attachmentId);
        }
        return attachment;
    }

    @Transactional(rollbackFor = TicketNotFound.class)
    public void delete(long id) throws TicketNotFound {
        Ticket deletedTicket = tRepo.findById(id).orElse(null);
        if (deletedTicket == null) {
            throw new TicketNotFound(id);
        }
        tRepo.delete(deletedTicket);
    }

    @Transactional(rollbackFor = AttachmentNotFound.class)
    public void deleteAttachment(long ticketId, UUID attachmentId)
            throws TicketNotFound, AttachmentNotFound {
        Ticket ticket = tRepo.findById(ticketId).orElse(null);
        if (ticket == null) {
            throw new TicketNotFound(ticketId);
        }
        for (Attachment attachment : ticket.getAttachments()) {
            if (attachment.getId().equals(attachmentId)) {
                ticket.deleteAttachment(attachment);
                tRepo.save(ticket);
                return;
            }
        }
        throw new AttachmentNotFound(attachmentId);
    }

    @Transactional
    public long createTicket(String customerName, String subject,
                             String body, List<MultipartFile> attachments)
            throws IOException {
        Ticket ticket = new Ticket();
        ticket.setCustomerName(customerName);
        ticket.setSubject(subject);
        ticket.setBody(body);

        for (MultipartFile filePart : attachments) {
            Attachment attachment = new Attachment();
            attachment.setName(filePart.getOriginalFilename());
            attachment.setMimeContentType(filePart.getContentType());
            attachment.setContents(filePart.getBytes());
            attachment.setTicket(ticket);
            if (attachment.getName() != null && attachment.getName().length() > 0
                    && attachment.getContents() != null
                    && attachment.getContents().length > 0) {
                ticket.getAttachments().add(attachment);
            }
        }
        Ticket savedTicket = tRepo.save(ticket);
        return savedTicket.getId();
    }

    @Transactional(rollbackFor = TicketNotFound.class)
    public void updateTicket(long id, String subject,
                             String body, List<MultipartFile> attachments)
            throws IOException, TicketNotFound {
        Ticket updatedTicket = tRepo.findById(id).orElse(null);
        if (updatedTicket == null) {
            throw new TicketNotFound(id);
        }
        updatedTicket.setSubject(subject);
        updatedTicket.setBody(body);
        for (MultipartFile filePart : attachments) {
            Attachment attachment = new Attachment();
            attachment.setName(filePart.getOriginalFilename());
            attachment.setMimeContentType(filePart.getContentType());
            attachment.setContents(filePart.getBytes());
            attachment.setTicket(updatedTicket);
            if (attachment.getName() != null && attachment.getName().length() > 0
                    && attachment.getContents() != null
                    && attachment.getContents().length > 0) {
                updatedTicket.getAttachments().add(attachment);
            }
        }
        tRepo.save(updatedTicket);
    }
}